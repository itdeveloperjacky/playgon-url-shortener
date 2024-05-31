package com.playgon.controller;

import com.playgon.config.SecurityConfig;
import com.playgon.model.UrlMappings;
import com.playgon.repository.UrlMappingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatisticsController.class)
@Import(SecurityConfig.class)
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlMappingsRepository urlMappingsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTopUrls() throws Exception {
        UrlMappings url1 = new UrlMappings();
        url1.setShortUrl("short1");
        url1.setLongUrl("https://example1.com");
        url1.setAccessCount(100);

        UrlMappings url2 = new UrlMappings();
        url2.setShortUrl("short2");
        url2.setLongUrl("https://example2.com");
        url2.setAccessCount(200);

        when(urlMappingsRepository.findTop10ByOrderByAccessCountDesc()).thenReturn(Arrays.asList(url1, url2));

        mockMvc.perform(get("/api/stats/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortUrl", is("short1")))
                .andExpect(jsonPath("$[0].longUrl", is("https://example1.com")))
                .andExpect(jsonPath("$[0].accessCount", is(100)))
                .andExpect(jsonPath("$[1].shortUrl", is("short2")))
                .andExpect(jsonPath("$[1].longUrl", is("https://example2.com")))
                .andExpect(jsonPath("$[1].accessCount", is(200)));
    }

    @Test
    void testGetUrlAccessCount() throws Exception {
        UrlMappings urlMapping = new UrlMappings();
        urlMapping.setShortUrl("short1");
        urlMapping.setLongUrl("https://example1.com");
        urlMapping.setAccessCount(100);

        when(urlMappingsRepository.findByShortUrl("short1")).thenReturn(Optional.of(urlMapping));

        mockMvc.perform(get("/api/stats/count/short1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(100)));
    }

    @Test
    void testGetUrlAccessCount_NotFound() throws Exception {
        when(urlMappingsRepository.findByShortUrl("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/stats/count/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", is("URL not found")));
    }
}
