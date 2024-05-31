package com.playgon.integration;

import com.playgon.PlaygonBackendApplication;
import com.playgon.model.UrlMappings;
import com.playgon.repository.UrlMappingsRepository;
import org.springframework.core.env.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PlaygonBackendApplication.class) // Use your actual main application class
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
public class StatisticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment env;

    @Autowired
    private UrlMappingsRepository urlMappingsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void contextLoads() {
        System.out.println("Datasource URL: " + env.getProperty("spring.datasource.url"));
    }

    @Test
    void testGetTopUrls() throws Exception {
        UrlMappings url1 = new UrlMappings();
        url1.setShortUrl("short1");
        url1.setLongUrl("https://example1.com");
        url1.setAccessCount(100);
        url1.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        UrlMappings url2 = new UrlMappings();
        url2.setShortUrl("short2");
        url2.setLongUrl("https://example2.com");
        url2.setAccessCount(200);
        url2.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        urlMappingsRepository.save(url1);
        urlMappingsRepository.save(url2);

        mockMvc.perform(get("/api/stats/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortUrl", is("short2")))
                .andExpect(jsonPath("$[0].longUrl", is("https://example2.com")))
                .andExpect(jsonPath("$[0].accessCount", is(200)))
                .andExpect(jsonPath("$[1].shortUrl", is("short1")))
                .andExpect(jsonPath("$[1].longUrl", is("https://example1.com")))
                .andExpect(jsonPath("$[1].accessCount", is(100)));
    }

    @Test
    void testGetUrlAccessCount() throws Exception {
        UrlMappings urlMapping = new UrlMappings();
        urlMapping.setShortUrl("short1");
        urlMapping.setLongUrl("https://example1.com");
        urlMapping.setAccessCount(100);
        urlMapping.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        urlMappingsRepository.save(urlMapping);

        mockMvc.perform(get("/api/stats/count/short1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(100)));
    }

    @Test
    void testGetUrlAccessCount_NotFound() throws Exception {
        mockMvc.perform(get("/api/stats/count/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", is("URL not found")));
    }
}
