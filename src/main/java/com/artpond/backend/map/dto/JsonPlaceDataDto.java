package com.artpond.backend.map.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonPlaceDataDto {
    @JsonProperty("osm_id")
    private Long osmId;

    @JsonProperty("osm_type")
    private String osmType; 

    @JsonProperty("lat")
    private String lat;

    @JsonProperty("lon")
    private String lon;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("name")
    private String name;
}
