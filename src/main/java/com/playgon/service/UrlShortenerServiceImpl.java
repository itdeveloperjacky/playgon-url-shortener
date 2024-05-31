package com.playgon.service;

import com.playgon.model.UrlMappings;
import com.playgon.repository.UrlMappingsRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private final UrlMappingsRepository urlMappingRepository;

    @Autowired
    public UrlShortenerServiceImpl(UrlMappingsRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    @Override
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

    @Override
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

    private String generateShortUrl() {
        return RandomStringUtils.randomAlphanumeric(6);
    }

    private String sanitizeUrl(String url) {
        // Basic sanitization logic to remove harmful characters
        return url.replaceAll("[^a-zA-Z0-9:/?&.=\\-_%]", "");
    }
}