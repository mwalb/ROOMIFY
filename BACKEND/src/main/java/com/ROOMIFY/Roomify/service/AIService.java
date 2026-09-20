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

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.1-flash-image-preview}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIRoomResponse generateRoomArrangement(AIRoomRequest request, byte[] imageBytes) throws Exception {
        String prompt = constructPrompt(request);
        
        // Base64 encode the image
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Prepare the request body for Gemini API
        Map<String, Object> body = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();

        // Part 1: Text prompt
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        parts.add(textPart);

        // Part 2: Image data
        Map<String, Object> imagePart = new HashMap<>();
        Map<String, String> inlineData = new HashMap<>();
        inlineData.put("mime_type", "image/jpeg");
        inlineData.put("data", base64Image);
        imagePart.put("inline_data", inlineData);
        parts.add(imagePart);

        content.put("parts", parts);
        contents.add(content);
        body.put("contents", contents);

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
                throw new RuntimeException("AI Quota exceeded. Please wait a moment and try again.");
            }
            throw new RuntimeException("AI Generation failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Gemini API Error Body: " + e.getMessage());
            throw new RuntimeException("AI Generation failed: " + e.getMessage());
        }
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
                // 1. Check for inline base64 data
                if (part.has("inline_data")) {
                    return "data:" + part.path("inline_data").path("mime_type").asText() + ";base64," + 
                           part.path("inline_data").path("data").asText();
                }
                
                // 2. Check for URI (some models return a hosted URL)
                if (part.has("file_data")) {
                    return part.path("file_data").path("file_uri").asText();
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
