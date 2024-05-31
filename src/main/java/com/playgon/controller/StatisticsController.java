package com.playgon.controller;

import com.playgon.model.UrlMappings;
import com.playgon.repository.UrlMappingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for handling URL statistics.
 */
@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    private final UrlMappingsRepository urlMappingRepository;

    /**
     * Constructor to inject the UrlMappingsRepository dependency.
     *
     * @param urlMappingRepository the URL mappings repository
     */
    @Autowired
    public StatisticsController(UrlMappingsRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    /**
     * Gets the top 10 accessed URLs.
     *
     * @return a list of UrlMappings representing the top accessed URLs
     */
    @GetMapping("/top")
    public List<UrlMappings> getTopUrls() {
        // Implement logic to return top accessed URLs
        return urlMappingRepository.findTop10ByOrderByAccessCountDesc();
    }

    /**
     * Gets the access count for a specific short URL.
     *
     * @param shortUrl the short URL
     * @return the access count of the short URL or a 404 status if not found
     */
    @GetMapping("/count/{shortUrl}")
    public ResponseEntity<?> getUrlAccessCount(@PathVariable String shortUrl) {
        // Implement logic to return the count of accesses for a given short URL
        Optional<UrlMappings> mapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (mapping.isPresent()) {
            return ResponseEntity.ok(mapping.get().getAccessCount());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("URL not found");
        }
    }
}
