package com.playgon.controller;

import com.playgon.config.SecurityConfig;
import com.playgon.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers  = UrlShortenerController.class)
@Import(SecurityConfig.class) // Import the security configuration for the tests
public class UrlShortenerControllerTest {

    @MockBean
    private UrlShortenerService urlShortenerService;

    @Autowired
    private MockMvc mockMvc;

    @Value("${app.base-url}")
    private String baseUrl;

    @Test
    void testHello() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello world"));
    }

    @Test
    void testShortenUrl() throws Exception {
        String longUrl = "https://example.com";
        String shortUrl = "abc123";

        // Mock the service method
        when(urlShortenerService.createShortUrl(anyString())).thenReturn(shortUrl);

        // Perform the POST request
        mockMvc.perform(post("/shorten")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longUrl\": \"" + longUrl + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value("http://localhost/" + shortUrl));
    }

    @Test
    void testShortenUrl_InvalidUrl() throws Exception {
        String invalidUrl = "invalid_url";

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longUrl\": \"" + invalidUrl + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"shortUrl\":\"Invalid URL format\"}"));
    }

    @Test
    void testRedirectUrl() throws Exception {
        String shortUrl = "abc123";
        String longUrl = "https://example.com";

        when(urlShortenerService.getOriginalUrl(shortUrl)).thenReturn(longUrl);

        mockMvc.perform(get("/" + shortUrl))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testRedirectUrl_NotFound() throws Exception {
        String shortUrl = "nonexistent";

        when(urlShortenerService.getOriginalUrl(shortUrl)).thenReturn(null);

        mockMvc.perform(get("/" + shortUrl))
                .andExpect(status().isNotFound());
    }
}
