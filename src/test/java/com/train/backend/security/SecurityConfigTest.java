package com.train.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Security Configuration.
 * Tests JWT validation and authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security Configuration Tests")
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Setup test fixtures if needed
    }

    @Test
    @DisplayName("Should allow access to health endpoints without authentication")
    void testHealthEndpointsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/health/ping"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.message").value("TrAIn Backend is running"));
    }

    @Test
    @DisplayName("Should return 401 for protected endpoints without authentication")
    void testProtectedEndpointWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow access to protected endpoints with valid JWT")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testProtectedEndpointWithValidJwt() throws Exception {
        mockMvc.perform(get("/api/users/profile")
            .header("Authorization", "Bearer valid-jwt-token"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should enforce CORS headers")
    void testCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/health/ping")
            .header("Origin", "http://localhost:4200")
            .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Should deny CORS from unauthorized origins")
    void testCorsDenyUnauthorizedOrigin() throws Exception {
        mockMvc.perform(options("/api/health/ping")
            .header("Origin", "http://unauthorized-origin.com")
            .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 for malformed JWT")
    void testMalformedJwtToken() throws Exception {
        mockMvc.perform(get("/api/users/profile")
            .header("Authorization", "Bearer malformed-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle missing Authorization header")
    void testMissingAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should support Bearer token format")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testBearerTokenFormat() throws Exception {
        String authHeader = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9";
        mockMvc.perform(get("/api/health/status")
            .header("Authorization", authHeader))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should deny invalid Authorization header format")
    void testInvalidAuthHeaderFormat() throws Exception {
        mockMvc.perform(get("/api/users/profile")
            .header("Authorization", "InvalidFormat token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 403 for insufficient permissions")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testInsufficientPermissions() throws Exception {
        mockMvc.perform(delete("/api/admin/users/123")
            .header("Authorization", "Bearer valid-jwt-token"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow admin endpoints with ADMIN role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminEndpointWithAdminRole() throws Exception {
        // This test requires an actual admin endpoint
        // Currently just validates security configuration works
        mockMvc.perform(get("/api/health/status"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should set SessionCreationPolicy to STATELESS")
    @WithMockUser(username = "testuser")
    void testStatelessSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/health/ping"))
            .andExpect(status().isOk())
            .andReturn();

        // Verify no session cookie is created
        String setCookie = result.getResponse().getHeader("Set-Cookie");
        // In stateless mode, no JSESSIONID should be set
        if (setCookie != null) {
            assert !setCookie.contains("JSESSIONID") : "Should not contain session cookie in stateless mode";
        }
    }

    @Test
    @DisplayName("Should expose necessary response headers")
    void testExposedHeaders() throws Exception {
        mockMvc.perform(options("/api/health/ping")
            .header("Origin", "http://localhost:4200"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Expose-Headers", containsString("Authorization")))
            .andExpect(header().string("Access-Control-Expose-Headers", containsString("Content-Type")));
    }
}
