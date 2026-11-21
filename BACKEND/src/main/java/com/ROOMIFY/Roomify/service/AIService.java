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

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIRoomResponse generateRoomArrangement(AIRoomRequest request, byte[] imageBytes) throws Exception {
        if (apiKey == null || apiKey.isBlank() || "disabled".equals(apiKey) || "none".equals(apiKey)) {
            throw new RuntimeException("AI Features are currently disabled: GEMINI_API_KEY is not configured.");
        }
        String prompt = constructPrompt(request);

        // Prepare the request body for Gemini API
        Map<String, Object> body = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();

        // Part 1: Text prompt
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        parts.add(textPart);

        // Part 2: Image data (optional if no source photo was provided)
        if (imageBytes != null && imageBytes.length > 0) {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            Map<String, Object> imagePart = new HashMap<>();
            Map<String, String> inlineData = new HashMap<>();
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", base64Image);
            imagePart.put("inline_data", inlineData);
            parts.add(imagePart);
        }

        content.put("parts", parts);
        contents.add(content);
        body.put("contents", contents);
        // Ask for image output as well as text (required by image-capable Gemini models)
        body.put("generationConfig", Map.of("responseModalities", List.of("TEXT", "IMAGE")));

        // Configure generation config (if needed for image generation/output)
        // Note: As of now, standard Gemini 1.5 Flash returns text. 
        // If "gemini-3.1-flash-image-preview" supports image output, it might return a base64 image or a URL.
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                System.out.println("Gemini API Response: " + responseEntity.getBody());
                JsonNode root = objectMapper.readTree(responseEntity.getBody());

                // Check for errors in the response
                if (root.has("error")) {
                    throw new RuntimeException("Gemini API Error: " + root.path("error").path("message").asText());
                }

                String generatedImageUrl = extractImageUrl(root);
                String recommendation = extractText(root);

                if (generatedImageUrl == null) {
                    System.err.println("Warning: Gemini returned text but no image data. Ensure the model supports image output.");
                }

                return AIRoomResponse.builder()
                        .generatedImageUrl(generatedImageUrl)
                        .recommendation(recommendation)
                        .style(request.getStyle())
                        .build();
            } else {
                throw new RuntimeException("Gemini API call failed: " + responseEntity.getStatusCode());
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new RuntimeException("AI quota reached. Please wait a minute and try again.");
            }
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("The configured AI model (" + model + ") is unavailable. Ask the administrator to update GEMINI_MODEL.");
            }
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST || e.getStatusCode() == HttpStatus.FORBIDDEN || e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException(buildFriendlyGeminiError(e.getStatusCode().value(), e.getResponseBodyAsString()));
            }
            throw new RuntimeException("AI service returned HTTP " + e.getStatusCode().value() + ". Please try again.");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Gemini API Error Body: " + e.getMessage());
            throw new RuntimeException("Could not reach the AI service. Please check your connection and try again.");
        }
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
