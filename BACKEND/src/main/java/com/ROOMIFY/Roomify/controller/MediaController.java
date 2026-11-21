package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.model.MediaAsset;
import com.ROOMIFY.Roomify.service.MediaStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class MediaController {

    @Autowired
    private MediaStorageService mediaStorageService;

    private static byte[] cachedPlaceholderJpg;
    private static byte[] cachedFaviconPng;

    @GetMapping("/favicon.ico")
    public ResponseEntity<byte[]> getFavicon() {
        byte[] icon = getOrCreateFaviconPng();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(7)))
                .body(icon);
    }

    @GetMapping("/uploads/**")
    public ResponseEntity<byte[]> serveUploadedMedia(
            HttpServletRequest request,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {

        String requestUri = request.getRequestURI();
        Optional<MediaAsset> optAsset = mediaStorageService.findMedia(requestUri);

        if (optAsset.isPresent()) {
            MediaAsset asset = optAsset.get();
            byte[] data = asset.getData();
            String contentTypeStr = MediaStorageService.resolveContentType(requestUri, asset.getContentType());
            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(contentTypeStr);
            } catch (Exception e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            // Support HTTP Range requests for video streaming (MP4, WebM, MOV)
            if (rangeHeader != null && rangeHeader.startsWith("bytes=") && contentTypeStr.startsWith("video/")) {
                return servePartialVideo(data, mediaType, rangeHeader);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentLength(data.length);
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.setCacheControl(CacheControl.maxAge(java.time.Duration.ofHours(24)).cachePublic());
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        }

        // If an older image is missing from ephemeral disk, return a valid JPEG placeholder
        // instead of a 404 JSON error so Kamel / browser decoders never fail with "Failed to decode 153 bytes to a bitmap".
        String lower = requestUri.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
            byte[] fallbackJpg = getOrCreatePlaceholderJpg(requestUri);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(fallbackJpg.length);
            headers.setCacheControl(CacheControl.noCache());
            return new ResponseEntity<>(fallbackJpg, headers, HttpStatus.OK);
        }

        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<byte[]> servePartialVideo(byte[] data, MediaType mediaType, String rangeHeader) {
        try {
            int fileSize = data.length;
            String rangeValue = rangeHeader.trim().substring("bytes=".length());
            String[] parts = rangeValue.split("-");
            int start = parts[0].isEmpty() ? 0 : Integer.parseInt(parts[0]);
            int end = (parts.length > 1 && !parts[1].isEmpty()) ? Integer.parseInt(parts[1]) : (fileSize - 1);

            if (start < 0) start = 0;
            if (end >= fileSize) end = fileSize - 1;
            if (start > end) {
                start = 0;
                end = fileSize - 1;
            }

            byte[] slice = Arrays.copyOfRange(data, start, end + 1);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentLength(slice.length);
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
            return new ResponseEntity<>(slice, headers, HttpStatus.PARTIAL_CONTENT);
        } catch (Exception e) {
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(data.length)
                    .body(data);
        }
    }

    private synchronized byte[] getOrCreatePlaceholderJpg(String requestUri) {
        if (cachedPlaceholderJpg != null) {
            return cachedPlaceholderJpg;
        }
        try {
            int width = 800;
            int height = 520;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Brand Navy to Indigo gradient background
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(26, 35, 126),
                    width, height, new Color(57, 73, 171)
            );
            g.setPaint(gradient);
            g.fillRect(0, 0, width, height);

            // Subtle decorative circles
            g.setColor(new Color(255, 255, 255, 18));
            g.fillOval(width - 220, -60, 280, 280);
            g.fillOval(-80, height - 200, 260, 260);

            // Center house / room icon outline
            int cx = width / 2;
            int cy = height / 2 - 25;
            g.setColor(new Color(255, 255, 255, 40));
            g.fillRoundRect(cx - 56, cy - 56, 112, 112, 28, 28);

            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int[] roofX = {cx - 32, cx, cx + 32};
            int[] roofY = {cy - 4, cy - 30, cy - 4};
            g.drawPolyline(roofX, roofY, 3);
            g.drawRect(cx - 22, cy - 4, 44, 32);
            g.drawRect(cx - 7, cy + 10, 14, 18);

            // Brand title
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            FontMetrics fm = g.getFontMetrics();
            String brand = "ROOMIFY";
            g.drawString(brand, (width - fm.stringWidth(brand)) / 2, cy + 88);

            g.setColor(new Color(224, 231, 255));
            g.setFont(new Font("SansSerif", Font.PLAIN, 16));
            FontMetrics fmSub = g.getFontMetrics();
            String sub = "Verified Living Space & Home Marketplace";
            g.drawString(sub, (width - fmSub.stringWidth(sub)) / 2, cy + 116);

            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            cachedPlaceholderJpg = baos.toByteArray();
            return cachedPlaceholderJpg;
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private synchronized byte[] getOrCreateFaviconPng() {
        if (cachedFaviconPng != null) {
            return cachedFaviconPng;
        }
        try {
            int size = 32;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(26, 35, 126));
            g.fillRoundRect(0, 0, size, size, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            FontMetrics fm = g.getFontMetrics();
            String letter = "R";
            g.drawString(letter, (size - fm.stringWidth(letter)) / 2, ((size - fm.getHeight()) / 2) + fm.getAscent());
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            cachedFaviconPng = baos.toByteArray();
            return cachedFaviconPng;
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
