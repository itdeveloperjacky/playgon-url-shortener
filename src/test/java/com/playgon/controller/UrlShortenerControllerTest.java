package com.playgon.controller;

import com.playgon.config.SecurityConfig;
import com.playgon.model.UrlRequest;
import com.playgon.service.UrlShortenerService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UrlShortenerController.class)
@Import(SecurityConfig.class) // Import the security configuration for the tests
@ActiveProfiles("test") // Use a test profile if needed
public class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlShortenerService urlShortenerService;

    @Value("${app.base-url}")
    private String baseUrl;

    @MockBean
    private Bucket bucket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.bucket = Bucket4j.builder()
                .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))))
                .build();
    }

    @Test
    public void testHello() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello world"));
    }

    @Test
    public void testShortenUrl_Success() throws Exception {
        UrlRequest request = new UrlRequest();
        request.setLongUrl("https://example.com");
        when(urlShortenerService.createShortUrl(anyString())).thenReturn("abc123");

        mockMvc.perform(MockMvcRequestBuilders.post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longUrl\": \"https://example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value("http://localhost/" + "abc123"));
    }

    @Test
    public void testShortenUrl_InvalidUrl() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longUrl\": \"invalid-url\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.shortUrl").value("Invalid URL format"));
    }

    @Test
    public void testRedirectUrl_Success() throws Exception {
        when(urlShortenerService.getOriginalUrl(anyString())).thenReturn("https://example.com");

        mockMvc.perform(MockMvcRequestBuilders.get("/abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://example.com"));
    }

    @Test
    public void testRedirectUrl_NotFound() throws Exception {
        when(urlShortenerService.getOriginalUrl(anyString())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/abc123"))
                .andExpect(status().isNotFound());
    }

}