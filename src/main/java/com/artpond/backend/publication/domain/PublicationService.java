package com.artpond.backend.publication.domain;

import com.artpond.backend.image.domain.Image;
import com.artpond.backend.image.dto.ImageResponseDto;
import com.artpond.backend.publication.dto.PublicationRequestDto;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.publication.infrastructure.PublicationRepository;
import com.artpond.backend.tag.domain.Tag;
import com.artpond.backend.tag.infrastructure.TagRepository;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;
import com.artpond.backend.user.dto.UserResponseDto;
import com.artpond.backend.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public Publication findPublicationById(Long id) {
        return publicationRepository.findById(id).orElseThrow();
    }

    public PublicationResponseDto createPublication(PublicationRequestDto dto, String username) {
            User user = userRepository.findByUsername(username).orElseThrow();
            Publication publication = new Publication();
            publication.setDescription(dto.getDescription());
            publication.setAuthor(user);
            List<Tag> tagEntities = dto.getTags().stream()
                    .map(tagName -> tagRepository.findByName(tagName)
                            .orElseGet(() -> {
                                Tag newTag = new Tag();
                                newTag.setName(tagName);
                                return tagRepository.save(newTag);
                            })
                    ).collect(Collectors.toList());
            publication.setTags(tagEntities);
            List<Image> imageEntities = dto.getImages().stream()
                .map(imgDto -> {
                    Image img = new Image();
                    img.setUrl(imgDto.getUrl());
                    img.setPublication(publication);
                    return img;
                })
                .collect(Collectors.toList());
            publication.setImages(imageEntities);

            return modelMapper.map(publicationRepository.save(publication), PublicationResponseDto.class);
    }

    public Page<PublicationResponseDto> getAllPublications(Pageable pageable) {
        return publicationRepository.findAll(pageable).map(pub ->
        {
            PublicationResponseDto dto = new PublicationResponseDto();
            dto.setId(pub.getId());
            dto.setDescription(pub.getDescription());
            dto.setAuthor(modelMapper.map(pub.getAuthor(), UserResponseDto.class));
            dto.setImages(pub.getImages().stream().map(img -> modelMapper.map(img, ImageResponseDto.class)).collect(Collectors.toList()));
            dto.setTags(pub.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
            return dto;
        });
    }

    public Page<PublicationResponseDto> getPublicationsByTag(String tagName, Pageable pageable) {
        Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        Page<Publication> publications = publicationRepository.findByTagsContaining(tag, pageable);

        return publications.map(pub -> modelMapper.map(pub, PublicationResponseDto.class));
    }

    public PublicationResponseDto getPublicationById(Long id) {
        Publication publication = publicationRepository.findById(id).orElseThrow();
        PublicationResponseDto dto = new PublicationResponseDto();
        dto.setId(publication.getId());
        dto.setDescription(publication.getDescription());
        dto.setAuthor(modelMapper.map(publication.getAuthor(), UserResponseDto.class));
        dto.setImages(publication.getImages().stream().map(img -> modelMapper.map(img, ImageResponseDto.class)).collect(Collectors.toList()));
        dto.setTags(publication.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
        return dto;
    }

    public PublicationResponseDto saveImages(Long id, List<Image> images) {
        Publication publication = publicationRepository.findById(id).orElseThrow(() -> new RuntimeException("Publication not found"));
        publication.setImages(images);
        return modelMapper.map(publicationRepository.save(publication), PublicationResponseDto.class);
    }
}