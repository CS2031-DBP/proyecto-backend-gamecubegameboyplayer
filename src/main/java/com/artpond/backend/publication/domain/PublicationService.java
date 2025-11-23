package com.artpond.backend.publication.domain;

import com.artpond.backend.definitions.exception.ForbiddenException;
import com.artpond.backend.definitions.exception.NotFoundException;
import com.artpond.backend.image.domain.Image;
import com.artpond.backend.image.dto.ImageResponseDto;
import com.artpond.backend.image.dto.ImageUploadDto;
import com.artpond.backend.image.domain.ImageService;
import com.artpond.backend.map.dto.PlaceDataDto;
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
import com.artpond.backend.user.domain.Role;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.dto.PublicUserDto;
import com.artpond.backend.user.exception.UserNotFoundException;
import com.artpond.backend.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final ImageService imageService;
    private final ApplicationEventPublisher eventPublisher;

    public Publication findPublicationById(Long id) {
        return publicationRepository.findById(id).orElseThrow();
    }

    private PublicationResponseDto toDto(Publication pub) {
        PublicationResponseDto dto = new PublicationResponseDto();
        dto.setId(pub.getId());
        dto.setDescription(pub.getDescription());
        dto.setContentWarning(pub.getContentWarning());
        dto.setMachineGenerated(pub.getMachineGenerated());
        dto.setCreationDate(pub.getCreationDate());
        dto.setAuthor(modelMapper.map(pub.getAuthor(), PublicUserDto.class));
        dto.setImages(
                pub.getImages().stream()
                        .map(img -> modelMapper.map(img, ImageResponseDto.class))
                        .collect(toList()));
        dto.setTags(
                pub.getTags().stream()
                        .map(tg -> modelMapper.map(tg, TagsResponseDto.class))
                        .collect(toList()));
        dto.setPlace(modelMapper.map(pub.getPlace(), PlaceDataDto.class));
        return dto;
    }

    @Transactional
    public PublicationCreatedDto createPublication(PublicationRequestDto dto, List<MultipartFile> imageFiles,
            String username) {

        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new PublicationCreationException("At least one image is required");
        }

        if (imageFiles.size() > 5) {
            throw new PublicationCreationException("Cannot upload more than 5 images");
        }

        if ((dto.getOsmId() != null && dto.getOsmType() == null) ||
                (dto.getOsmId() == null && dto.getOsmType() != null)) {
            throw new PublicationCreationException("osmId and osmType must both be provided or both be null");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException());

        Publication publication = new Publication();
        publication.setDescription(dto.getDescription());
        publication.setAuthor(user);
        publication.setContentWarning(dto.getContentWarning() != null ? dto.getContentWarning() : false);
        publication.setMachineGenerated(dto.getMachineGenerated() != null ? dto.getMachineGenerated() : false);

        // Handle tags
        List<Tag> tagEntities = dto.getTags().stream()
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return tagRepository.save(newTag);
                        }))
                .collect(toList());
        publication.setTags(tagEntities);
        Publication savedPublication = publicationRepository.save(publication);

        try {
            List<ImageUploadDto> uploadResults = imageService.uploadImagesForPublication(
                    imageFiles, username, savedPublication.getId());

            List<Image> imageEntities = uploadResults.stream()
                    .map(imgResult -> {
                        Image img = new Image();
                        img.setUrl(imgResult.getPublicUrl()); // Watermarked public URL
                        img.setCleanFileKey(imgResult.getCleanFileKey()); // Clean version key
                        img.setPublicFileKey(imgResult.getPublicFileKey()); // For deletion
                        img.setPublication(savedPublication);
                        return img;
                    })
                    .collect(toList());

            savedPublication.setImages(imageEntities);
            publicationRepository.save(savedPublication);

        } catch (IOException e) {
            // Cleanup: delete the publication if image upload fails
            publicationRepository.delete(savedPublication);
            throw new PublicationCreationException("Failed to upload images: " + e.getMessage());
        }

        if (dto.getOsmId() != null && dto.getOsmType() != null) {
            eventPublisher.publishEvent(new PublicationCreatedEvent(
                    this,
                    savedPublication.getId(),
                    dto.getOsmId(),
                    dto.getOsmType()));
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
            if (currentUser == null)
                throw new ForbiddenException("Can't see explicit content without logging in.");
            else if (currentUser.getShowExplicit() != true)
                throw new ForbiddenException("User has explicit content disabled.");
        }

        return toDto(publication);
    }

    public PublicationResponseDto patchPublication(Long id, Map<String, Object> updates, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        Publication pub = publicationRepository.findById(id)
                .orElseThrow(() -> new PublicationNotFoundException());

        if (!pub.getAuthor().getUserId().equals(user.getUserId()))
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
                case "contentWarning" -> pub.setContentWarning((Boolean) value);
            }
        });

        return toDto(publicationRepository.save(pub));
    }

    @Transactional
    public Void deletePublicationById(Long id, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new PublicationNotFoundException());

        if (!publication.getAuthor().getUserId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("No se puede eliminar esta publicacion.");
        }

        // Delete all images from S3
        List<String> cleanKeys = publication.getImages().stream()
                .map(Image::getCleanFileKey)
                .collect(toList());
        List<String> publicKeys = publication.getImages().stream()
                .map(Image::getPublicFileKey)
                .collect(toList());

        imageService.deleteMultipleImages(cleanKeys, publicKeys);

        publicationRepository.deleteById(id);

        return null;
    }
}