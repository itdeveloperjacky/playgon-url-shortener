package com.playgon.service;

import com.playgon.model.UrlMappings;
import com.playgon.repository.UrlMappingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UrlShortenerServiceImplTest {

    @Mock
    private UrlMappingsRepository urlMappingsRepository;

    @InjectMocks
    private UrlShortenerServiceImpl urlShortenerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateShortUrl_Success() {
        String longUrl = "https://example.com";
        String shortUrl = "abc123";

        // Mock the behavior of the repository
        when(urlMappingsRepository.save(any(UrlMappings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock the generateShortUrl method
        UrlShortenerServiceImpl spyService = spy(urlShortenerService);
        doReturn(shortUrl).when(spyService).generateShortUrl();

        String result = spyService.createShortUrl(longUrl);

        assertEquals(shortUrl, result);
        verify(urlMappingsRepository, times(1)).save(any(UrlMappings.class));
    }

    @Test
    public void testGetOriginalUrl_Success() {
        String shortUrl = "abc123";
        String longUrl = "https://example.com";

        UrlMappings urlMapping = new UrlMappings();
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setLongUrl(longUrl);
        urlMapping.setAccessCount(0);

        // Mock the behavior of the repository
        when(urlMappingsRepository.findByShortUrl(shortUrl)).thenReturn(Optional.of(urlMapping));

        String result = urlShortenerService.getOriginalUrl(shortUrl);

        assertEquals(longUrl, result);
        assertEquals(1, urlMapping.getAccessCount());
        verify(urlMappingsRepository, times(1)).save(urlMapping);
    }

    @Test
    public void testGetOriginalUrl_NotFound() {
        String shortUrl = "nonexistent";

        // Mock the behavior of the repository
        when(urlMappingsRepository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        String result = urlShortenerService.getOriginalUrl(shortUrl);

        assertEquals(null, result);
        verify(urlMappingsRepository, never()).save(any(UrlMappings.class));
    }

    @Test
    public void testFallbackCreateShortUrl() {
        String longUrl = "https://example.com";
        String expectedMessage = "Service is currently unavailable. Please try again later.";

        String result = urlShortenerService.fallbackCreateShortUrl(longUrl, new Exception("Test Exception"));

        assertEquals(expectedMessage, result);
    }

    @Test
    public void testFallbackGetOriginalUrl() {
        String shortUrl = "abc123";
        String expectedMessage = "Service is currently unavailable. Please try again later.";

        String result = urlShortenerService.fallbackGetOriginalUrl(shortUrl, new Exception("Test Exception"));

        assertEquals(expectedMessage, result);
    }

}
