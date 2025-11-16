package com.artpond.backend.map.dto;

import lombok.Data;

@Data
public class PlaceDataDto {
    private Long osmId;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
}
