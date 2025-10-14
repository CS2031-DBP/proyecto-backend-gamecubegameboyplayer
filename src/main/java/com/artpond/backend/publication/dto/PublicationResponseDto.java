package com.artpond.backend.publication.dto;

import com.artpond.backend.user.dto.UserDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

@Data
public class PublicationResponseDto {
    private Long id;
    @NotNull
    private UserDto owner;
    @Size(max=250)
    private String description;
    @CreationTimestamp
    private Instant creationDate;
    @NotNull
    private Boolean contentWarning;
    @Size(max = 15)
    private List<String> tags = new ArrayList<>();
}
