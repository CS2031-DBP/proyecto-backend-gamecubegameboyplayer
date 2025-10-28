package com.artpond.backend.publication.dto;

import com.artpond.backend.image.dto.ImageRequestDto;
import com.artpond.backend.image.dto.ImageResponseDto;
import lombok.Data;

import java.util.List;

@Data
public class PublicationRequestDto {
    private String description;
    private List<String> tags;
    private List<ImageRequestDto> images;
}
