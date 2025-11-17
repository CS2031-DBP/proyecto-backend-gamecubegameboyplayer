package com.artpond.backend.tag.domain;

import com.artpond.backend.tag.infrastructure.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    public void deleteTagById(Long id) {
        tagRepository.deleteById(id);
    }
}
