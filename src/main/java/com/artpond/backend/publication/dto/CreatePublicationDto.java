package com.artpond.backend.publication.dto;

import com.artpond.backend.image.domain.Image;
import com.artpond.backend.tag.domain.Tag;
import lombok.Data;

import java.util.List;

@Data
public class CreatePublicationDto {
    private String description;
    private List<String> tags;
    private List<Image> images;
    // add image
}
