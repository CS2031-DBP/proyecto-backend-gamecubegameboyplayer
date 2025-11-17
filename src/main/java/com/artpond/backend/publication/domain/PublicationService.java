package com.artpond.backend.publication.domain;

import com.artpond.backend.definitions.exception.ForbiddenException;
import com.artpond.backend.definitions.exception.NotFoundException;
import com.artpond.backend.image.domain.Image;
import com.artpond.backend.image.dto.ImageResponseDto;
import com.artpond.backend.publication.dto.PublicationCreatedDto;
import com.artpond.backend.publication.dto.PublicationRequestDto;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.publication.event.PublicationCreatedEvent;
import com.artpond.backend.publication.exception.PublicationCreationException;
import com.artpond.backend.publication.exception.PublicationNotFoundException;
import com.artpond.backend.publication.infrastructure.PublicationRepository;
import com.artpond.backend.tag.domain.Tag;
import com.artpond.backend.tag.dto.TagsResponseDto;
import com.artpond.backend.tag.infrastructure.TagRepository;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.dto.PublicUserDto;
import com.artpond.backend.user.exception.UserNotFoundException;
import com.artpond.backend.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    private final ApplicationEventPublisher eventPublisher;

    public Publication findPublicationById(Long id) {
        return publicationRepository.findById(id).orElseThrow();
    }

    private PublicationResponseDto toDto(Publication pub) {
        PublicationResponseDto dto = new PublicationResponseDto();
        dto.setId(pub.getId());
        dto.setDescription(pub.getDescription());
        dto.setAuthor(modelMapper.map(pub.getAuthor(), PublicUserDto.class));
        dto.setImages(
                pub.getImages().stream()
                        .map(img -> modelMapper.map(img, ImageResponseDto.class))
                        .collect(toList()));
        dto.setTags(
                pub.getTags().stream()
                        .map(tg -> modelMapper.map(tg, TagsResponseDto.class))
                        .collect(toList()));
        return dto;
    }

    public PublicationCreatedDto createPublication(PublicationRequestDto dto, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException());
        Publication publication = new Publication();
        publication.setDescription(dto.getDescription());
        publication.setAuthor(user);
        List<Tag> tagEntities = dto.getTags().stream()
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return tagRepository.save(newTag);
                        }))
                .collect(toList());
        publication.setTags(tagEntities);
        List<Image> imageEntities = dto.getImages().stream()
                .map(imgDto -> {
                    Image img = new Image();
                    img.setUrl(imgDto.getUrl());
                    img.setPublication(publication);
                    return img;
                })
                .collect(toList());
        publication.setImages(imageEntities);

        if ((dto.getOsmId() != null && dto.getOsmType() == null) ||
            (dto.getOsmId() == null && dto.getOsmType() != null)) {
            throw new PublicationCreationException("osmId and osmType must both be provided or both be null");
        }

        Publication savedPublication = publicationRepository.save(publication);

        if (dto.getOsmId() != null && dto.getOsmType() != null) {
            eventPublisher.publishEvent(new PublicationCreatedEvent(
                this, 
                savedPublication.getId(), 
                dto.getOsmId(), 
                dto.getOsmType()
            ));
        }

        return modelMapper.map(savedPublication, PublicationCreatedDto.class);
    }

    public Page<PublicationResponseDto> getAllPublications(Pageable pageable, User currentUser) {
        return publicationRepository.findAll(pageable).map(this::toDto);
    }

    public Page<PublicationResponseDto> getPublicationsByTag(String tagName, Pageable pageable, User currentUser) {
        Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new NotFoundException("Tag does not exist"));

        return publicationRepository.findByTagsContaining(tag, pageable).map(this::toDto);
    }

    public PublicationResponseDto getPublicationById(Long id, User currentUser) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new PublicationNotFoundException());
        
        if (publication.getContentWarning() == true) {
            if (currentUser == null) throw new ForbiddenException("Can't see explicit content without logging in.");
            else if (currentUser.getShowExplicit() != true) throw new ForbiddenException("User has explicit content disabled.");
        }

        return toDto(publication);
    }

    public PublicationResponseDto saveImages(Long id, List<Image> images) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new PublicationNotFoundException());
        publication.setImages(images);
        return modelMapper.map(publicationRepository.save(publication), PublicationResponseDto.class);
    }

    public PublicationResponseDto patchPublication(Long id, Map<String, Object> updates, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        Publication pub = publicationRepository.findById(id)
                .orElseThrow(() -> new PublicationNotFoundException());
        
        if (pub.getAuthor().getUserId() != user.getUserId())
            throw new ForbiddenException("No puedes editar una publicacion que no te pertenece.");

        updates.forEach((key, value) -> {
            switch (key) {
                case "description" -> pub.setDescription((String) value);
                case "tags" -> {
                    List<String> tagNames = ((List<?>) value).stream()
                            .map(Object::toString).toList();
                    pub.setTags(tagNames.stream()
                            .map(tagName -> tagRepository.findByName(tagName)
                                    .orElseGet(() -> {
                                        Tag newTag = new Tag();
                                        newTag.setName(tagName);
                                        return tagRepository.save(newTag);
                                    }))
                            .toList());
                }
            }
        });

        return toDto(publicationRepository.save(pub));
    }

    public Void deletePublicationById(Long id, Long userId) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new PublicationNotFoundException());
        if (publication.getAuthor().getUserId() == userId) {
            publicationRepository.deleteById(id);
        } else {
            throw new NotFoundException("No se puede eliminar esta publicacion.");
        }
        return null;
    }
}