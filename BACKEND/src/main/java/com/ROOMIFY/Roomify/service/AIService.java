package com.ROOMIFY.Roomify.service;

import com.ROOMIFY.Roomify.dto.ai.AIRoomRequest;
import com.ROOMIFY.Roomify.dto.ai.AIRoomResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash-image}")
    private String model;

    private static final List<String> IMAGE_MODEL_CANDIDATES = List.of(
            "gemini-2.5-flash-image",
            "gemini-3.1-flash-image-preview",
            "gemini-3.0-flash-image",
            "gemini-2.5-flash-image-preview",
            "gemini-2.0-flash-exp-image-generation"
    );

    private static final List<String> TEXT_FALLBACK_MODELS = List.of(
            "gemini-3.0-flash",
            "gemini-2.5-flash",
            "gemini-flash-latest"
    );

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIRoomResponse generateRoomArrangement(AIRoomRequest request, byte[] imageBytes) throws Exception {
        if (apiKey == null || apiKey.isBlank() || "disabled".equals(apiKey) || "none".equals(apiKey)) {
            throw new RuntimeException("AI Features are currently disabled: GEMINI_API_KEY is not configured.");
        }
        String prompt = constructPrompt(request);

        List<Map<String, Object>> parts = new ArrayList<>();
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        parts.add(textPart);

        if (imageBytes != null && imageBytes.length > 0) {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            Map<String, Object> imagePart = new HashMap<>();
            Map<String, String> inlineData = new HashMap<>();
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", base64Image);
            imagePart.put("inline_data", inlineData);
            parts.add(imagePart);
        }

        List<Map<String, Object>> contents = List.of(Map.of("parts", parts));

        // Build ordered model attempt list starting with configured model
        List<String> imageModels = new ArrayList<>();
        if (model != null && !model.isBlank()) {
            imageModels.add(stripModelsPrefix(model));
        }
        for (String candidate : IMAGE_MODEL_CANDIDATES) {
            if (!imageModels.contains(candidate)) {
                imageModels.add(candidate);
            }
        }
        List<String> textModels = new ArrayList<>(TEXT_FALLBACK_MODELS);

        // Dynamically query Google's /v1beta/models catalog for this key so new models (e.g. gemini-3.x) are auto-discovered
        for (String liveModel : discoverAvailableModels()) {
            if (liveModel.contains("image") && !imageModels.contains(liveModel)) {
                imageModels.add(liveModel);
            } else if (liveModel.contains("flash") && !textModels.contains(liveModel)) {
                textModels.add(liveModel);
            }
        }

        RuntimeException lastError = null;
        Set<String> attempted = new HashSet<>();

        // 1. Try image-capable models first (with responseModalities = TEXT, IMAGE)
        for (int i = 0; i < imageModels.size(); i++) {
            String candidateModel = imageModels.get(i);
            if (!attempted.add("img:" + candidateModel)) continue;
            try {
                return callGeminiModel(candidateModel, contents, true, request.getStyle());
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                int code = e.getStatusCode().value();
                String bodyStr = e.getResponseBodyAsString();
                System.err.println("Gemini image model '" + candidateModel + "' failed (HTTP " + code + "): " + bodyStr);

                if (code == 401 || code == 403) {
                    throw new RuntimeException(buildFriendlyGeminiError(code, bodyStr));
                }
                // If Google tells us "Please update your code to use models/<xyz>", try <xyz> next!
                String suggested = extractSuggestedModel(bodyStr);
                if (suggested != null && !imageModels.contains(suggested)) {
                    imageModels.add(suggested);
                    if (!textModels.contains(suggested)) textModels.add(0, suggested);
                }
                if (code == 429) {
                    lastError = new RuntimeException("AI quota reached. Please wait a minute and try again.");
                    continue;
                }
                lastError = new RuntimeException(buildFriendlyGeminiError(code, bodyStr));
            } catch (RuntimeException e) {
                lastError = e;
            }
        }

        // 2. Fallback to standard multimodal text models so the user still gets an AI layout plan
        for (int i = 0; i < textModels.size(); i++) {
            String textModel = textModels.get(i);
            if (!attempted.add("txt:" + textModel)) continue;
            try {
                return callGeminiModel(textModel, contents, false, request.getStyle());
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                int code = e.getStatusCode().value();
                String bodyStr = e.getResponseBodyAsString();
                System.err.println("Gemini fallback model '" + textModel + "' failed (HTTP " + code + "): " + bodyStr);
                if (code == 401 || code == 403) {
                    throw new RuntimeException(buildFriendlyGeminiError(code, bodyStr));
                }
                String suggested = extractSuggestedModel(bodyStr);
                if (suggested != null && !textModels.contains(suggested)) {
                    textModels.add(suggested);
                }
                lastError = new RuntimeException(buildFriendlyGeminiError(code, bodyStr));
            } catch (RuntimeException e) {
                lastError = e;
            }
        }

        if (lastError != null) {
            throw lastError;
        }
        throw new RuntimeException("Could not reach the AI service. Please check your connection and try again.");
    }

    private String stripModelsPrefix(String name) {
        if (name == null) return "";
        String trimmed = name.trim();
        return trimmed.startsWith("models/") ? trimmed.substring("models/".length()) : trimmed;
    }

    /**
     * Parses a model recommendation from Google's deprecation error message, e.g.
     * "Please update your code to use models/gemini-3.0-flash for the latest features".
     */
    private String extractSuggestedModel(String errorBody) {
        if (errorBody == null) return null;
        int idx = errorBody.indexOf("use models/");
        if (idx < 0) return null;
        String tail = errorBody.substring(idx + "use models/".length());
        int end = 0;
        while (end < tail.length()) {
            char c = tail.charAt(end);
            if (Character.isLetterOrDigit(c) || c == '-' || c == '.' || c == '_') {
                end++;
            } else {
                break;
            }
        }
        String candidate = tail.substring(0, end).trim();
        while (candidate.endsWith(".")) {
            candidate = candidate.substring(0, candidate.length() - 1);
        }
        return candidate.isEmpty() ? null : candidate;
    }

    /**
     * Queries Google's /v1beta/models endpoint to discover which Gemini models are currently enabled for this API key.
     */
    private List<String> discoverAvailableModels() {
        List<String> discovered = new ArrayList<>();
        try {
            String listUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey.trim();
            ResponseEntity<String> resp = restTemplate.getForEntity(listUrl, String.class);
            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                JsonNode modelsNode = objectMapper.readTree(resp.getBody()).path("models");
                for (JsonNode m : modelsNode) {
                    String name = stripModelsPrefix(m.path("name").asText(""));
                    if (!name.startsWith("gemini-")) continue;
                    boolean supportsGenerate = false;
                    for (JsonNode method : m.path("supportedGenerationMethods")) {
                        if ("generateContent".equals(method.asText())) {
                            supportsGenerate = true;
                            break;
                        }
                    }
                    if (supportsGenerate) {
                        discovered.add(name);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Note: Could not list Gemini models dynamically: " + e.getMessage());
        }
        return discovered;
    }

    private AIRoomResponse callGeminiModel(
            String targetModel,
            List<Map<String, Object>> contents,
            boolean requestImageModality,
            String style
    ) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);
        if (requestImageModality) {
            body.put("generationConfig", Map.of("responseModalities", List.of("TEXT", "IMAGE")));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + targetModel + ":generateContent?key=" + apiKey.trim();

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entity, String.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Gemini API call failed: " + responseEntity.getStatusCode());
        }

        JsonNode root = objectMapper.readTree(responseEntity.getBody());
        if (root.has("error")) {
            throw new RuntimeException("Gemini API Error: " + root.path("error").path("message").asText());
        }

        String generatedImageUrl = extractImageUrl(root);
        String recommendation = extractText(root);
        System.out.println("✅ Gemini succeeded with model '" + targetModel + "' (image=" + (generatedImageUrl != null) + ")");

        return AIRoomResponse.builder()
                .generatedImageUrl(generatedImageUrl)
                .recommendation(recommendation)
                .style(style)
                .build();
    }

    /**
     * Logs the raw Gemini error on the server and returns a short, readable message for the UI
     * (never dumping raw JSON to the end user).
     */
    private String buildFriendlyGeminiError(int status, String rawBody) {
        System.err.println("Gemini HTTP " + status + ": " + rawBody);
        String apiMessage = "";
        String reason = "";
        try {
            JsonNode err = objectMapper.readTree(rawBody).path("error");
            apiMessage = err.path("message").asText("");
            for (JsonNode d : err.path("details")) {
                if (d.has("reason")) {
                    reason = d.path("reason").asText("");
                    break;
                }
            }
        } catch (Exception ignored) {}

        if (status == 403 || "ACCESS_DENIED".equalsIgnoreCase(reason) || "SERVICE_DISABLED".equalsIgnoreCase(reason)
                || apiMessage.toLowerCase().contains("permission denied")) {
            return "Gemini API access is denied for the configured key (HTTP 403). Enable the Generative Language API on this key's Google Cloud project, remove any IP/referrer restrictions on the key, or create a fresh key at aistudio.google.com.";
        }
        if ("API_KEY_INVALID".equalsIgnoreCase(reason) || apiMessage.toLowerCase().contains("api key not valid")) {
            return "The configured GEMINI_API_KEY is not valid. Create a new key at aistudio.google.com and set it with `fly secrets set GEMINI_API_KEY=...`.";
        }
        if (apiMessage.toLowerCase().contains("response modalities") || apiMessage.toLowerCase().contains("image")) {
            return "The selected Gemini model (" + model + ") does not support image generation on this key. Try setting GEMINI_MODEL to an image-enabled model.";
        }
        if (!apiMessage.isBlank()) {
            return "AI service rejected the request: " + apiMessage;
        }
        return "AI service rejected the request (HTTP " + status + "). Check GEMINI_API_KEY and model access.";
    }

    private String constructPrompt(AIRoomRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an interior-space visualization assistant for RoomifyTZ.\n\n");
        sb.append("Use the uploaded room photograph as the primary source image.\n\n");
        sb.append("Preserve the existing room architecture, walls, floor, ceiling, doors, windows, permanent fixtures, room proportions and camera perspective.\n");
        sb.append("Do not redesign or reconstruct the room.\n\n");
        sb.append("Add the requested furniture realistically inside the existing room.\n\n");
        
        sb.append("Furniture requested:\n");
        for (String item : request.getFurniture()) {
            sb.append("- ").append(item).append("\n");
        }
        sb.append("\n");

        sb.append("Preferred style: ").append(request.getStyle()).append("\n");
        
        if (request.getRoomWidth() != null && request.getRoomLength() != null) {
            sb.append("Room dimensions: ").append(request.getRoomWidth()).append("m x ").append(request.getRoomLength()).append("m\n");
        }
        
        if (request.getAdditionalInstructions() != null && !request.getAdditionalInstructions().isEmpty()) {
            sb.append("Additional instructions: ").append(request.getAdditionalInstructions()).append("\n");
        }
        
        sb.append("\nArrange furniture according to realistic interior-design principles. ");
        sb.append("Maintain realistic furniture scale relative to the room. ");
        sb.append("Keep doors and windows accessible. Maintain a practical walking path. ");
        sb.append("Do not place furniture through walls. Do not change the room's structural elements. ");
        sb.append("Do not invent additional rooms. Do not significantly alter the original architecture. ");
        sb.append("Generate a photorealistic visualization showing how this exact room could look after furnishing.");
        
        return sb.toString();
    }

    private String extractImageUrl(JsonNode root) {
        try {
            JsonNode candidates = root.path("candidates");
            if (candidates.isMissingNode() || candidates.size() == 0) return null;

            JsonNode content = candidates.get(0).path("content");
            if (content.isMissingNode()) return null;

            JsonNode parts = content.path("parts");
            if (parts.isMissingNode()) return null;

            for (JsonNode part : parts) {
                // 1. Inline base64 data (the REST API answers in camelCase; accept snake_case too)
                JsonNode inline = part.has("inlineData") ? part.path("inlineData")
                        : (part.has("inline_data") ? part.path("inline_data") : null);
                if (inline != null) {
                    String mime = inline.has("mimeType") ? inline.path("mimeType").asText()
                            : inline.path("mime_type").asText("image/png");
                    return "data:" + mime + ";base64," + inline.path("data").asText();
                }

                // 2. Check for URI (some models return a hosted URL)
                JsonNode fileData = part.has("fileData") ? part.path("fileData")
                        : (part.has("file_data") ? part.path("file_data") : null);
                if (fileData != null) {
                    return fileData.has("fileUri") ? fileData.path("fileUri").asText()
                            : fileData.path("file_uri").asText();
                }

                // 3. Fallback: Search for anything that looks like a data URI in text
                if (part.has("text")) {
                    String text = part.get("text").asText();
                    if (text.contains("data:image/")) {
                        int start = text.indexOf("data:image/");
                        int end = text.indexOf("\"", start);
                        if (end == -1) end = text.indexOf(" ", start);
                        if (end == -1) end = text.length();
                        return text.substring(start, end).trim();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting image URL: " + e.getMessage());
        }
        return null;
    }

    private String extractText(JsonNode root) {
        try {
            JsonNode parts = root.path("candidates").get(0).path("content").path("parts");
            for (JsonNode part : parts) {
                if (part.has("text")) {
                    return part.get("text").asText();
                }
            }
        } catch (Exception e) {
            return "Generation successful, but no description was provided.";
        }
        return "Arrangement complete.";
    }
}
