package com.ROOMIFY.Roomify.security;

import com.ROOMIFY.Roomify.Component.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:jwttestdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
public class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String secret;

    @Test
    @DisplayName("1. Public GET /api/rooms with no token returns 200 (Guest Access)")
    void testGetAllRooms_NoToken_Returns200GuestAccess() throws Exception {
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. Public GET /api/rooms with valid token authenticates user and returns 200")
    void testGetAllRooms_ValidBearerToken_Returns200Authenticated() throws Exception {
        String token = jwtUtil.generateToken("superadmin@roomify.com", "SUPER_ADMIN");
        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("3. Public GET /api/rooms with invalid token returns 200 (retains guest access)")
    void testGetAllRooms_InvalidToken_RetainsGuestAccess() throws Exception {
        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer invalid.jwt.token.structure"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("4. Protected endpoint without token returns 4xx Client Error (Access Denied)")
    void testProtectedEndpoint_NoToken_Returns4xx() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("5. Protected endpoint with invalid token returns 4xx Client Error (Access Denied)")
    void testProtectedEndpoint_InvalidToken_Returns4xx() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer invalid.jwt.token.structure"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("6. Protected endpoint with expired token returns 4xx Client Error (Access Denied)")
    void testProtectedEndpoint_ExpiredToken_Returns4xx() throws Exception {
        String expiredToken = Jwts.builder()
                .setSubject("superadmin@roomify.com")
                .claim("role", "SUPER_ADMIN")
                .setIssuedAt(new Date(System.currentTimeMillis() - 100000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // Expired
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .compact();

        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("7. Sensitive POST /api/rooms without token returns 4xx Client Error")
    void testPostRoom_NoToken_Returns4xx() throws Exception {
        mockMvc.perform(post("/api/rooms"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("8. Role-restricted admin endpoint with non-admin token returns 403 Forbidden")
    void testAdminEndpoint_NonAdminToken_Returns403() throws Exception {
        String tenantToken = jwtUtil.generateToken("tenant@roomify.com", "TENANT");
        mockMvc.perform(get("/api/admin-management/stats")
                        .header("Authorization", "Bearer " + tenantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("9. Role-restricted admin endpoint with Super Admin token returns 200 OK")
    void testAdminEndpoint_SuperAdminToken_Returns200() throws Exception {
        String superAdminToken = jwtUtil.generateToken("superadmin@roomify.com", "SUPER_ADMIN");
        mockMvc.perform(get("/api/admin-management/stats")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk());
    }
}
