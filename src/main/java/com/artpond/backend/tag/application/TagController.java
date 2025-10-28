package com.artpond.backend.tag.application;

import com.artpond.backend.publication.domain.PublicationService;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.tag.domain.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(name = "tag")
public class TagController {
    private final TagService tagService;


}
