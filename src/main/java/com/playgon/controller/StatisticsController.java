package com.playgon.controller;

import com.playgon.model.UrlMappings;
import com.playgon.repository.UrlMappingsRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    private final UrlMappingsRepository urlMappingRepository;

    @Autowired
    public StatisticsController(UrlMappingsRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    @GetMapping("/top")
    public List<UrlMappings> getTopUrls() {
        // Implement logic to return top accessed URLs
        return urlMappingRepository.findTop10ByOrderByAccessCountDesc();
    }

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
