package com.artpond.backend.image.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageUploadDto {
    private String cleanFileKey;
    private String publicFileKey;
    private String publicUrl;
}
