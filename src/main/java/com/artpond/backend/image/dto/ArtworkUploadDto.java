package com.artpond.backend.image.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ArtworkUploadDto {
    private List<ImageUploadDto> images;
}