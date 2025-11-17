package com.artpond.backend.tag.application;

import com.artpond.backend.tag.domain.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(name = "tag")
public class TagController {
    private final TagService tagService;

    @DeleteMapping
    public ResponseEntity<?> deleteTag(@PathVariable Long id) {
        tagService.deleteTagById(id);
        return ResponseEntity.noContent().build();
    }
}