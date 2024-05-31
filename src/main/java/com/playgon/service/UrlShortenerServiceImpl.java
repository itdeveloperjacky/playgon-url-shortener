package com.playgon.service;

import com.playgon.model.UrlMappings;
import com.playgon.repository.UrlMappingsRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service implementation for URL shortening and retrieval.
 */
@Service
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private final UrlMappingsRepository urlMappingRepository;

    /**
     * Constructor to inject the UrlMappingsRepository dependency.
     *
     * @param urlMappingRepository the URL mappings repository
     */
    @Autowired
    public UrlShortenerServiceImpl(UrlMappingsRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    /**
     * Creates a shortened URL for the given long URL. Uses a circuit breaker to handle failures gracefully.
     *
     * @param longUrl the original long URL
     * @return the shortened URL
     */
    @Override
    @CircuitBreaker(name = "urlShortenerService", fallbackMethod = "fallbackCreateShortUrl")
    public String createShortUrl(String longUrl) {
        String shortUrl = generateShortUrl();
        UrlMappings mapping = new UrlMappings();
        mapping.setLongUrl(sanitizeUrl(longUrl));
        mapping.setShortUrl(shortUrl);
        mapping.setAccessCount(0);
        mapping.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        urlMappingRepository.save(mapping);
        return shortUrl;
    }

    /**
     * Retrieves the original URL corresponding to the given shortened URL. Uses a circuit breaker to handle failures gracefully.
     *
     * @param shortUrl the shortened URL
     * @return the original long URL, or null if not found
     */
    @Override
    @CircuitBreaker(name = "urlShortenerService", fallbackMethod = "fallbackGetOriginalUrl")
    public String getOriginalUrl(String shortUrl) {
        Optional<UrlMappings> mapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (mapping.isPresent()) {
            UrlMappings urlMapping = mapping.get();
            urlMapping.setAccessCount(urlMapping.getAccessCount() + 1);
            urlMappingRepository.save(urlMapping); // Update access count
            return urlMapping.getLongUrl();
        }
        return null;
    }

    /**
     * Generates a random alphanumeric string to be used as a short URL.
     *
     * @return the generated short URL
     */
    String generateShortUrl() {
        return RandomStringUtils.randomAlphanumeric(6);
    }

    /**
     * Sanitizes the given URL to remove harmful characters.
     *
     * @param url the URL to sanitize
     * @return the sanitized URL
     */
    private String sanitizeUrl(String url) {
        // Basic sanitization logic to remove harmful characters
        return url.replaceAll("[^a-zA-Z0-9:/?&.=\\-_%]", "");
    }

    /**
     * Fallback method for createShortUrl in case of circuit breaker triggering.
     *
     * @param longUrl the original long URL
     * @param t       the throwable causing the fallback
     * @return a fallback message indicating service unavailability
     */
    String fallbackCreateShortUrl(String longUrl, Throwable t) {
        return "Service is currently unavailable. Please try again later.";
    }

    /**
     * Fallback method for getOriginalUrl in case of circuit breaker triggering.
     *
     * @param shortUrl the shortened URL
     * @param t        the throwable causing the fallback
     * @return a fallback message indicating service unavailability
     */
    String fallbackGetOriginalUrl(String shortUrl, Throwable t) {
        return "Service is currently unavailable. Please try again later.";
    }
}
