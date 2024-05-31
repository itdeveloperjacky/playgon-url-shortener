package com.playgon.service;

public interface UrlShortenerService {
    String createShortUrl(String longUrl);
    String getOriginalUrl(String shortUrl);
}
