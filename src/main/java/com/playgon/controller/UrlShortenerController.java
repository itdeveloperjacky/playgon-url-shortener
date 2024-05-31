package com.playgon.controller;

import com.playgon.model.UrlRequest;
import com.playgon.model.UrlResponse;
import com.playgon.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

@RestController
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
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

        if (!isValidUrl(request.getLongUrl())) {
            return ResponseEntity.badRequest().body(new UrlResponse("Invalid URL format"));
        }

        String shortUrl = urlShortenerService.createShortUrl(request.getLongUrl());

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();


        return ResponseEntity.ok(new UrlResponse(baseUrl + "/" + shortUrl));
    }

    /**
     * Retrieves the original URL corresponding to the given shortened URL.
     *
     * @param shortUrl the shortened URL
     * @return the original URL, or null if not found
     */
    @GetMapping("/{shortUrl}")
    public void redirectUrl(@PathVariable String shortUrl, HttpServletResponse response) throws IOException {

        String longUrl = urlShortenerService.getOriginalUrl(shortUrl);
        if (longUrl != null) {
            response.sendRedirect(longUrl);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
