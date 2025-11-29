package com.artpond.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_places_coordinates ON places USING GIST (coordinates);");
            log.info("Database indexes verified/created successfully");
        } catch (Exception e) {
            log.error("Failed to create database indexes", e);
            // No lanzar excepción para no bloquear el startup
        }
    }
}
