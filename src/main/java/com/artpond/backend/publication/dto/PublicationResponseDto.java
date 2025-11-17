package com.artpond.backend.publication.dto;

import com.artpond.backend.image.dto.ImageResponseDto;
import com.artpond.backend.tag.dto.TagsResponseDto;
import com.artpond.backend.user.dto.PublicUserDto;
import lombok.Data;

import java.util.List;

@Data
public class PublicationResponseDto {
    private Long id;
    private String description;
    private PublicUserDto author;
    private List<ImageResponseDto> images;
    private List<TagsResponseDto> tags;
}
