package com.artpond.backend.map.dto;

import lombok.Data;

@Data
public class PlaceMapSummaryDto {
    private Long id;
    private String name;
    private double latitude;
    private double longitude;
    private Long postCount;
}