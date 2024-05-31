package com.playgon.integration;

import com.playgon.PlaygonBackendApplication;
import com.playgon.repository.UrlMappingsRepository;
import com.playgon.service.UrlShortenerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PlaygonBackendApplication.class) // Use your actual main application class
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
public class UrlShortenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingsRepository urlMappingsRepository;

    @Autowired
    private UrlShortenerServiceImpl urlShortenerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHello() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello world"));
    }

    @Test
    void testShortenUrl() throws Exception {
        String longUrl = "https://example.com";

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longUrl\": \"" + longUrl + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").exists());
    }

    @Test
    void testShortenUrl_InvalidUrl() throws Exception {
        String invalidUrl = "invalid_url";

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longUrl\": \"" + invalidUrl + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.shortUrl", is("Invalid URL format")));
    }

    @Test
    void testRedirectUrl() throws Exception {
        String longUrl = "https://example.com";
        String shortUrl = urlShortenerService.createShortUrl(longUrl);

        mockMvc.perform(get("/" + shortUrl))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testRedirectUrl_NotFound() throws Exception {
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound());
    }
}
