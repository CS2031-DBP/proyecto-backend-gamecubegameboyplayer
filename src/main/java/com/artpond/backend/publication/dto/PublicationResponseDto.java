package com.artpond.backend.publication.dto;

import com.artpond.backend.image.dto.ImageResponseDto;
import com.artpond.backend.map.dto.PlaceDataDto;
import com.artpond.backend.tag.dto.TagsResponseDto;
import com.artpond.backend.user.dto.PublicUserDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PublicationResponseDto {
    private Long id;
    private String description;
    private PublicUserDto author;

    private Boolean contentWarning;
    private Boolean machineGenerated;
    private LocalDateTime creationDate;
    
    private List<ImageResponseDto> images;
    private List<TagsResponseDto> tags;
    private PlaceDataDto place;
}
