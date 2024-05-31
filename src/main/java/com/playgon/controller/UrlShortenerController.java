package com.playgon.controller;

import com.playgon.model.UrlRequest;
import com.playgon.model.UrlResponse;
import com.playgon.service.UrlShortenerService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

@RestController
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;
    private final Bucket bucket;

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        this.bucket = Bucket4j.builder().addLimit(limit).build();
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    /**
     * Creates a shortened URL for the given long URL.
     *
     * @param request the original long URL embedded in request body
     * @return the shortened URL
     */
    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> shortenUrl(@RequestBody UrlRequest request) {
        if (bucket.tryConsume(1)) {
            if (!isValidUrl(request.getLongUrl())) {
                return ResponseEntity.badRequest().body(new UrlResponse("Invalid URL format"));
            }

            String shortUrl = urlShortenerService.createShortUrl(request.getLongUrl());
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

            return ResponseEntity.ok(new UrlResponse(baseUrl + "/" + shortUrl));
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new UrlResponse("Too many requests"));
        }
    }

    /**
     * Retrieves the original URL corresponding to the given shortened URL.
     *
     * @param shortUrl the shortened URL
     * @return the original URL, or null if not found
     */
    @GetMapping("/{shortUrl}")
    public void redirectUrl(@PathVariable String shortUrl, HttpServletResponse response) throws IOException {
        if (bucket.tryConsume(1)) {
            String longUrl = urlShortenerService.getOriginalUrl(shortUrl);
            if (longUrl != null) {
                response.sendRedirect(longUrl);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(429, "Too many requests");
        }
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
