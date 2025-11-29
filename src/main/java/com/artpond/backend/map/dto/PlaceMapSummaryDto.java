package com.artpond.backend.map.dto;

public interface PlaceMapSummaryDto {
    Long getId();
    String getName();
    Double getLatitude();
    Double getLongitude();
    Long getPostCount();
}