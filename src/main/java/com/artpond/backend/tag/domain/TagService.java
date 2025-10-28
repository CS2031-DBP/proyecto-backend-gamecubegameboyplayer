package com.artpond.backend.tag.domain;

import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.tag.infrastructure.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    public Page<PublicationResponseDto> getPublicationsByTag() {

        return null;
    }
}
