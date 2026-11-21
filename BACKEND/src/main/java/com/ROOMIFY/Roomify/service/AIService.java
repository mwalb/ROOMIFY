package com.ROOMIFY.Roomify.service;

import com.ROOMIFY.Roomify.dto.ai.AIRoomRequest;
import com.ROOMIFY.Roomify.dto.ai.AIRoomResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

@Service
public class AIService {

    @Autowired
    private MediaStorageService mediaStorageService;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash-image}")
    private String model;

    private static final List<String> IMAGE_MODEL_CANDIDATES = List.of(
            "gemini-2.5-flash-image",
            "gemini-3.0-flash-image",
            "gemini-2.0-flash-exp-image-generation"
    );

    private static final List<String> TEXT_FALLBACK_MODELS = List.of(
            "gemini-3.8-flash",
            "gemini-3.0-flash",
            "gemini-2.5-flash"
    );

    private final RestTemplate geminiRestTemplate = createTimedRestTemplate(6000, 14000);
    private final RestTemplate quickRenderRestTemplate = createTimedRestTemplate(3500, 6500);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService bgExecutor = Executors.newCachedThreadPool();

    // Caches the last known working models so subsequent requests finish in a single ~3s call
    private volatile String cachedWorkingImageModel = null;
    private volatile String cachedWorkingTextModel = null;
    private volatile List<String> cachedDiscoveredModels = null;
    private volatile long discoveredModelsTimestamp = 0L;

    private static RestTemplate createTimedRestTemplate(int connectMs, int readMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectMs);
        factory.setReadTimeout(readMs);
        return new RestTemplate(factory);
    }

    public AIRoomResponse generateRoomArrangement(AIRoomRequest request, byte[] imageBytes) throws Exception {
        long startNs = System.nanoTime();
        if (apiKey == null || apiKey.isBlank() || "disabled".equals(apiKey) || "none".equals(apiKey)) {
            throw new RuntimeException("AI Features are currently disabled: GEMINI_API_KEY is not configured.");
        }
        String prompt = constructPrompt(request);

        List<Map<String, Object>> parts = new ArrayList<>();
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        parts.add(textPart);

        byte[] compactImage = downscaleImageForFastUpload(imageBytes);
        if (compactImage != null && compactImage.length > 0) {
            String base64Image = Base64.getEncoder().encodeToString(compactImage);
            Map<String, Object> imagePart = new HashMap<>();
            Map<String, String> inlineData = new HashMap<>();
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", base64Image);
            imagePart.put("inline_data", inlineData);
            parts.add(imagePart);
        }

        List<Map<String, Object>> contents = List.of(Map.of("parts", parts));

        // Pre-warm the visual fallback render in parallel so if the Gemini key only returns text,
        // the room render is already ready (zero extra wait time!)
        Future<String> fallbackRenderFuture = bgExecutor.submit(() -> fetchVisualFallbackImage(request));

        try {
            // Fast path: if we already found a working image model, try it first
            if (cachedWorkingImageModel != null) {
                try {
                    AIRoomResponse fastRes = callGeminiModel(cachedWorkingImageModel, contents, true, request.getStyle());
                    if (fastRes.getGeneratedImageUrl() != null && !fastRes.getGeneratedImageUrl().isBlank()) {
                        fallbackRenderFuture.cancel(true);
                        logLatency("cached-image:" + cachedWorkingImageModel, startNs);
                        return fastRes;
                    }
                } catch (Exception ignored) {
                    cachedWorkingImageModel = null;
                }
            }

            // Build concise candidate lists (max 2 image attempts + 1-2 text attempts to avoid multi-model lag)
            List<String> imageModels = new ArrayList<>();
            if (model != null && !model.isBlank()) {
                imageModels.add(stripModelsPrefix(model));
            }
            for (String candidate : IMAGE_MODEL_CANDIDATES) {
                if (!imageModels.contains(candidate)) {
                    imageModels.add(candidate);
                }
            }
            List<String> textModels = new ArrayList<>();
            if (cachedWorkingTextModel != null) {
                textModels.add(cachedWorkingTextModel);
            }
            for (String tm : TEXT_FALLBACK_MODELS) {
                if (!textModels.contains(tm)) {
                    textModels.add(tm);
                }
            }

            RuntimeException lastError = null;
            Set<String> attempted = new HashSet<>();
            String fallbackRecommendation = null;

            // 1. Try at most 2 image-capable Gemini models
            int imageAttempts = 0;
            for (int i = 0; i < imageModels.size() && imageAttempts < 2; i++) {
                String candidateModel = imageModels.get(i);
                if (!attempted.add("img:" + candidateModel)) continue;
                imageAttempts++;
                try {
                    AIRoomResponse res = callGeminiModel(candidateModel, contents, true, request.getStyle());
                    if (res.getGeneratedImageUrl() != null && !res.getGeneratedImageUrl().isBlank()) {
                        cachedWorkingImageModel = candidateModel;
                        fallbackRenderFuture.cancel(true);
                        logLatency("image:" + candidateModel, startNs);
                        return res;
                    }
                    // Model succeeded and returned a rich design recommendation without an inline image
                    if (res.getRecommendation() != null && !res.getRecommendation().isBlank()) {
                        fallbackRecommendation = res.getRecommendation();
                        cachedWorkingTextModel = candidateModel;
                        break; // We already have the recommendation! Skip redundant model calls.
                    }
                } catch (org.springframework.web.client.HttpClientErrorException e) {
                    int code = e.getStatusCode().value();
                    String bodyStr = e.getResponseBodyAsString();
                    System.err.println("Gemini image model '" + candidateModel + "' failed (HTTP " + code + ")");

                    if (code == 401 || code == 403) {
                        throw new RuntimeException(buildFriendlyGeminiError(code, bodyStr));
                    }
                    String suggested = extractSuggestedModel(bodyStr);
                    if (suggested != null) {
                        if (suggested.contains("image") && !imageModels.contains(suggested)) {
                            imageModels.add(i + 1, suggested);
                        } else if (!textModels.contains(suggested)) {
                            textModels.add(0, suggested);
                        }
                    }
                    lastError = new RuntimeException(buildFriendlyGeminiError(code, bodyStr));
                } catch (RuntimeException e) {
                    lastError = e;
                }
            }

            // 2. If Step 1 already gave us a recommendation, immediately attach the parallel render and return!
            if (fallbackRecommendation != null) {
                String renderedUrl = awaitFallbackRender(fallbackRenderFuture, request);
                logLatency("parallel-render+rec", startNs);
                return AIRoomResponse.builder()
                        .generatedImageUrl(renderedUrl)
                        .recommendation(fallbackRecommendation)
                        .style(request.getStyle())
                        .build();
            }

            // 3. Otherwise get text recommendation from the fastest available Gemini Flash model (in parallel with the render)
            for (int i = 0; i < textModels.size(); i++) {
                String textModel = textModels.get(i);
                if (!attempted.add("txt:" + textModel)) continue;
                try {
                    AIRoomResponse textRes = callGeminiModel(textModel, contents, false, request.getStyle());
                    cachedWorkingTextModel = textModel;
                    if (textRes.getGeneratedImageUrl() == null || textRes.getGeneratedImageUrl().isBlank()) {
                        textRes.setGeneratedImageUrl(awaitFallbackRender(fallbackRenderFuture, request));
                    }
                    logLatency("text:" + textModel, startNs);
                    return textRes;
                } catch (org.springframework.web.client.HttpClientErrorException e) {
                    int code = e.getStatusCode().value();
                    String bodyStr = e.getResponseBodyAsString();
                    System.err.println("Gemini text model '" + textModel + "' failed (HTTP " + code + ")");
                    if (code == 401 || code == 403) {
                        throw new RuntimeException(buildFriendlyGeminiError(code, bodyStr));
                    }
                    String suggested = extractSuggestedModel(bodyStr);
                    if (suggested != null && !textModels.contains(suggested)) {
                        textModels.add(i + 1, suggested);
                    } else if (i == textModels.size() - 1) {
                        // Last resort: auto-discover live models once per hour
                        for (String live : discoverAvailableModelsCached()) {
                            if (!textModels.contains(live)) textModels.add(live);
                        }
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
        } finally {
            fallbackRenderFuture.cancel(true);
        }
    }

    private void logLatency(String path, long startNs) {
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
        System.out.println("⚡ AI Room Arranger completed via [" + path + "] in " + elapsedMs + " ms");
    }

    /**
     * Downscales large source room photos to max 768px JPEG before sending to Gemini,
     * cutting payload size from 3-6 MB down to ~65 KB and saving 3-6 seconds of upload/token latency.
     */
    private byte[] downscaleImageForFastUpload(byte[] rawBytes) {
        if (rawBytes == null || rawBytes.length <= 120_000) return rawBytes;
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(rawBytes));
            if (src == null) return rawBytes;
            int maxDim = 768;
            int w = src.getWidth();
            int h = src.getHeight();
            if (w <= maxDim && h <= maxDim) return rawBytes;

            double scale = (w > h) ? ((double) maxDim / w) : ((double) maxDim / h);
            int targetW = Math.max(1, (int) Math.round(w * scale));
            int targetH = Math.max(1, (int) Math.round(h * scale));

            BufferedImage resized = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(src, 0, 0, targetW, targetH, Color.WHITE, null);
            g2d.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(resized, "jpg", out);
            return out.toByteArray();
        } catch (Exception e) {
            return rawBytes;
        }
    }

    private String awaitFallbackRender(Future<String> renderFuture, AIRoomRequest request) {
        try {
            String storedPath = renderFuture.get(4500, TimeUnit.MILLISECONDS);
            if (storedPath != null && !storedPath.isBlank()) {
                return storedPath;
            }
        } catch (Exception ignored) {
            // If downloading onto the server takes >4.5s, return the direct fast turbo URL immediately
            // so the client receives the response right away and streams the image in KamelImage!
        }
        return buildDirectTurboRenderUrl(request);
    }

    /**
     * Converts raw image bytes or a base64 data URI into a stored /uploads/ai/... URL served by MediaController
     * so KamelImage on Web/Android/iOS can load it reliably over HTTP.
     */
    private String persistGeneratedImage(byte[] bytes, String mimeType) {
        if (bytes == null || bytes.length == 0) return null;
        String mime = (mimeType != null && !mimeType.isBlank()) ? mimeType : "image/jpeg";
        String ext = mime.contains("png") ? ".png" : (mime.contains("webp") ? ".webp" : ".jpg");
        String path = "/uploads/ai/" + UUID.randomUUID() + ext;
        mediaStorageService.storeMedia(path, mime, bytes);
        return path;
    }

    private String buildDirectTurboRenderUrl(AIRoomRequest request) {
        String furnitureList = (request.getFurniture() != null && !request.getFurniture().isEmpty())
                ? String.join(", ", request.getFurniture())
                : "bed, wardrobe, study desk, curtains, warm lighting";
        String style = (request.getStyle() != null && !request.getStyle().isBlank()) ? request.getStyle() : "Modern";
        String extra = (request.getAdditionalInstructions() != null && !request.getAdditionalInstructions().isBlank())
                ? ", " + request.getAdditionalInstructions().trim()
                : "";
        String visualPrompt = "Photorealistic interior design photograph of a " + style
                + " residential room furnished with " + furnitureList + extra
                + ", realistic architecture, natural window light, wide angle lens";
        String encoded = java.net.URLEncoder.encode(visualPrompt, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");
        int seed = Math.abs(Objects.hash(style, furnitureList, extra)) % 99999;
        return "https://image.pollinations.ai/prompt/" + encoded
                + "?width=768&height=512&nologo=true&model=turbo&seed=" + seed;
    }

    /**
     * Generates a photorealistic furnished room visualization via Pollinations Turbo in the background
     * while Gemini is running, and stores it in MediaStorageService (/uploads/ai/...).
     */
    private String fetchVisualFallbackImage(AIRoomRequest request) {
        String genUrl = buildDirectTurboRenderUrl(request);
        try {
            ResponseEntity<byte[]> imgResp = quickRenderRestTemplate.getForEntity(genUrl, byte[].class);
            if (imgResp.getStatusCode() == HttpStatus.OK && imgResp.getBody() != null && imgResp.getBody().length > 1024) {
                return persistGeneratedImage(imgResp.getBody(), "image/jpeg");
            }
        } catch (Exception e) {
            System.err.println("Visual fallback render note (returning direct turbo stream URL): " + e.getMessage());
        }
        return genUrl;
    }

    private String stripModelsPrefix(String name) {
        if (name == null) return "";
        String trimmed = name.trim();
        return trimmed.startsWith("models/") ? trimmed.substring("models/".length()) : trimmed;
    }

    /**
     * Parses a model recommendation from Google's deprecation error message, e.g.
     * "Please update your code to use models/gemini-3.8-flash for the latest features".
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
     * Queries Google's /v1beta/models endpoint at most once per hour (only when fallback discovery is needed).
     */
    private List<String> discoverAvailableModelsCached() {
        long now = System.currentTimeMillis();
        if (cachedDiscoveredModels != null && (now - discoveredModelsTimestamp) < 3_600_000L) {
            return cachedDiscoveredModels;
        }
        List<String> discovered = new ArrayList<>();
        try {
            String listUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey.trim();
            ResponseEntity<String> resp = geminiRestTemplate.getForEntity(listUrl, String.class);
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
            cachedDiscoveredModels = discovered;
            discoveredModelsTimestamp = now;
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
        } else {
            body.put("generationConfig", Map.of("maxOutputTokens", 450, "temperature", 0.6));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + targetModel + ":generateContent?key=" + apiKey.trim();

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity = geminiRestTemplate.postForEntity(url, entity, String.class);

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
        sb.append("You are an interior-space visualization assistant for RoomifyTZ.\n");
        sb.append("Use the uploaded room photograph as the primary source image. Preserve existing walls, floor, windows, doors, and camera perspective.\n\n");
        sb.append("Furniture requested: ").append(String.join(", ", request.getFurniture())).append("\n");
        sb.append("Preferred style: ").append(request.getStyle()).append("\n");

        if (request.getRoomWidth() != null && request.getRoomLength() != null) {
            sb.append("Room dimensions: ").append(request.getRoomWidth()).append("m x ").append(request.getRoomLength()).append("m\n");
        }
        if (request.getAdditionalInstructions() != null && !request.getAdditionalInstructions().isEmpty()) {
            sb.append("Additional instructions: ").append(request.getAdditionalInstructions()).append("\n");
        }

        sb.append("\nProvide a concise, well-structured 4-point interior layout plan (using numbered bullet points) and generate a photorealistic visualization showing how this room looks after furnishing.");
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
                JsonNode inline = part.has("inlineData") ? part.path("inlineData")
                        : (part.has("inline_data") ? part.path("inline_data") : null);
                if (inline != null) {
                    String mime = inline.has("mimeType") ? inline.path("mimeType").asText()
                            : inline.path("mime_type").asText("image/png");
                    String b64 = inline.path("data").asText("");
                    if (!b64.isBlank()) {
                        try {
                            return persistGeneratedImage(Base64.getDecoder().decode(b64), mime);
                        } catch (Exception decodeEx) {
                            return "data:" + mime + ";base64," + b64;
                        }
                    }
                }

                JsonNode fileData = part.has("fileData") ? part.path("fileData")
                        : (part.has("file_data") ? part.path("file_data") : null);
                if (fileData != null) {
                    return fileData.has("fileUri") ? fileData.path("fileUri").asText()
                            : fileData.path("file_uri").asText();
                }

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
