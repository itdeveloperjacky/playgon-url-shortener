package com.playgon.repository;

import com.playgon.model.UrlMappings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.List;

@Repository
public interface UrlMappingsRepository extends JpaRepository<UrlMappings, Long> {
    Optional<UrlMappings> findByShortUrl(String shortUrl);

    // This will gather top 10 accessed URLs
    List<UrlMappings> findTop10ByOrderByAccessCountDesc();
}