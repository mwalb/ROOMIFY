package com.ROOMIFY.Roomify.controller;

import com.ROOMIFY.Roomify.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/price-ranges")
    public ResponseEntity<Map<String, Object>> getMostFrequentPriceRanges() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", analyticsService.getMostFrequentPriceRanges());
        } catch (Exception e) {
            System.err.println("Analytics price-ranges warning: " + e.getMessage());
            response.put("success", true);
            response.put("data", Collections.emptyList());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/most-booked-areas")
    public ResponseEntity<Map<String, Object>> getMostBookedAreas() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", analyticsService.getMostBookedAreas());
        } catch (Exception e) {
            System.err.println("Analytics most-booked-areas warning: " + e.getMessage());
            response.put("success", true);
            response.put("data", Collections.emptyList());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exact-prices")
    public ResponseEntity<Map<String, Object>> getMostFrequentExactPrices() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", analyticsService.getMostFrequentExactPrices());
        } catch (Exception e) {
            System.err.println("Analytics exact-prices warning: " + e.getMessage());
            response.put("success", true);
            response.put("data", Collections.emptyList());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly-trends")
    public ResponseEntity<Map<String, Object>> getMonthlyTrends() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", analyticsService.getMonthlyBookingTrends());
        } catch (Exception e) {
            System.err.println("Analytics monthly-trends warning: " + e.getMessage());
            response.put("success", true);
            response.put("data", Collections.emptyList());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-rooms")
    public ResponseEntity<Map<String, Object>> getTopRevenueRooms() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", analyticsService.getTopRevenueRooms());
        } catch (Exception e) {
            System.err.println("Analytics top-rooms warning: " + e.getMessage());
            response.put("success", true);
            response.put("data", Collections.emptyList());
        }
        return ResponseEntity.ok(response);
    }
}
