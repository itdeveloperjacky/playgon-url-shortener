package com.playgon.service;

import com.playgon.model.UrlMappings;
import com.playgon.repository.UrlMappingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("dev")
class UrlShortenerServiceImplTest {

    @Mock
    private UrlMappingsRepository urlMappingRepository;

    @InjectMocks
    private UrlShortenerServiceImpl urlShortenerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateShortUrl() {
        String longUrl = "https://example.com";
        String shortUrl = "abc123";

        when(urlMappingRepository.save(any(UrlMappings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = urlShortenerService.createShortUrl(longUrl);

        assertEquals(6, result.length()); // Assuming your short URL length is 6
        verify(urlMappingRepository, times(1)).save(any(UrlMappings.class));
    }

    @Test
    void testGetOriginalUrl() {
        String shortUrl = "abc123";
        String longUrl = "https://example.com";
        UrlMappings urlMapping = new UrlMappings();
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setLongUrl(longUrl);
        urlMapping.setAccessCount(0); // Initial access count

        when(urlMappingRepository.findByShortUrl(shortUrl)).thenReturn(Optional.of(urlMapping));
        when(urlMappingRepository.save(any(UrlMappings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = urlShortenerService.getOriginalUrl(shortUrl);
        assertEquals(longUrl, result);
        assertEquals(1, urlMapping.getAccessCount()); // Verify access count increment
        verify(urlMappingRepository, times(1)).save(urlMapping); // Verify save is called to update access count
    }

    @Test
    void testGetOriginalUrl_NotFound() {
        String shortUrl = "nonexistent";

        when(urlMappingRepository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        String result = urlShortenerService.getOriginalUrl(shortUrl);
        assertNull(result);
    }
}
