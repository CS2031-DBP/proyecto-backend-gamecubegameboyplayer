package com.artpond.backend.publication.domain;

import com.artpond.backend.definitions.exception.BadRequestException;
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
import com.artpond.backend.publication.infrastructure.FailedPlaceRepository;
import com.artpond.backend.publication.dto.FailedTaskDto;
import com.artpond.backend.publication.event.PublicationLikedEvent;
import com.artpond.backend.publication.event.PublicationModeratedEvent;
import com.artpond.backend.user.domain.Role;

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
    private final FailedPlaceRepository failedPlaceRepository;

    public Publication findPublicationById(Long id) {
        return publicationRepository.findById(id).orElseThrow();
    }

    private PublicationResponseDto toDto(Publication pub) {
        return toDto(pub, null);
    }

    private PublicationResponseDto toDto(Publication pub, User viewer) {
        PublicationResponseDto dto = new PublicationResponseDto();
        dto.setId(pub.getId());
        dto.setDescription(pub.getDescription());
        dto.setContentWarning(pub.getContentWarning());
        dto.setMachineGenerated(pub.getMachineGenerated());
        dto.setCreationDate(pub.getCreationDate());
        dto.setAuthor(modelMapper.map(pub.getAuthor(), PublicUserDto.class));
        
        dto.setImages(pub.getImages().stream().map(img -> {
            ImageResponseDto imgDto = new ImageResponseDto();
            imgDto.setId(img.getId());
            boolean showClean = false;
            if (viewer != null) {
                if (Boolean.FALSE.equals(pub.getHideCleanImage())) {
                    showClean = true;
                }
            }
            if (showClean) {
                imgDto.setUrl(imageService.generatePresignedUrl(img.getCleanFileKey()));
            } else {
                imgDto.setUrl(img.getUrl());
            }
            return imgDto;
        }).collect(toList()));
        
        dto.setTags(
            pub.getTags().stream()
                .map(tg -> modelMapper.map(tg, TagsResponseDto.class))
                .collect(toList())
        );
        
        // el lugar puede ser nulo, mientras que no este procesado o no se haya etiqutado
        dto.setPlace(pub.getPlace() != null ? 
            modelMapper.map(pub.getPlace(), PlaceDataDto.class) : null);
        
        return dto;
    }

    @Transactional
    public PublicationCreatedDto createPublication(PublicationRequestDto dto, List<MultipartFile> imageFiles, String username) {
        
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new PublicationCreationException("Se requiere almenos una imagen para subir una publicación");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException());

        int maxImagesAllowed = (user.getRole() == Role.ARTIST) 
                               ? ImageService.MAX_IMAGES_ARTIST 
                               : ImageService.MAX_IMAGES_USER;

        if (imageFiles.size() > maxImagesAllowed) {
            throw new PublicationCreationException(
                String.format("Tu rol actual (%s) solo permite subir hasta %d imágenes.", 
                user.getRole(), maxImagesAllowed)
            );
        }

        int maxTags = user.getRole() == Role.ARTIST ? 30 : 15;
        if (dto.getTags() != null && dto.getTags().size() > maxTags) {
             throw new BadRequestException(
                String.format("Tu rol actual (%s) solo permite %d etiquetas.", user.getRole(), maxTags)
            );
        }

        if ((dto.getOsmId() != null && dto.getOsmType() == null) ||
                (dto.getOsmId() == null && dto.getOsmType() != null)) {
            throw new PublicationCreationException("osmId and osmType must both be provided or both be null");
        }

        Publication publication = new Publication();
        publication.setDescription(dto.getDescription());
        publication.setAuthor(user);
        publication.setContentWarning(dto.getContentWarning() != null ? dto.getContentWarning() : false);
        publication.setMachineGenerated(dto.getMachineGenerated() != null ? dto.getMachineGenerated() : false);
        publication.setHideCleanImage(dto.getHideCleanImage() != null ? dto.getHideCleanImage() : false);

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
                    imageFiles, username, savedPublication.getId(), user.getRole());

            List<Image> imageEntities = uploadResults.stream()
                    .map(imgResult -> {
                        Image img = new Image();
                        img.setUrl(imgResult.getPublicUrl());
                        img.setCleanFileKey(imgResult.getCleanFileKey());
                        img.setPublicFileKey(imgResult.getPublicFileKey());
                        img.setPublication(savedPublication);
                        return img;
                    })
                    .collect(toList());

            savedPublication.setImages(imageEntities);
            publicationRepository.save(savedPublication);

        } catch (IOException | IllegalArgumentException e) {
            publicationRepository.delete(savedPublication);
            throw new PublicationCreationException("No se pudo subir imagen: " + e.getMessage());
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
        return publicationRepository.findAll(pageable).map(pub -> toDto(pub, currentUser));
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

        return toDto(publication, currentUser);
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

        return toDto(publicationRepository.save(pub), user);
    }

    @Transactional
    public Void deletePublicationById(Long id, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new PublicationNotFoundException());

        boolean isOwner = publication.getAuthor().getUserId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN || user.getRole() == Role.MODERATOR;

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("No se puede eliminar esta publicacion.");
        }

        if (isAdmin && !isOwner) {
            String preview = publication.getDescription() != null 
                    ? publication.getDescription().substring(0, Math.min(publication.getDescription().length(), 30)) 
                    : "Publicación #" + publication.getId();

            eventPublisher.publishEvent(new PublicationModeratedEvent(
                this, 
                publication.getAuthor(), 
                preview, 
                "Contenido eliminado por normas de la comunidad."
            ));
        }

        List<String> cleanKeys = publication.getImages().stream().map(Image::getCleanFileKey).collect(toList());
        List<String> publicKeys = publication.getImages().stream().map(Image::getPublicFileKey).collect(toList());
        imageService.deleteMultipleImages(cleanKeys, publicKeys);

        publicationRepository.deleteById(id);

        return null;
    }

    // heart systemo
    @Transactional
    public void toggleHeart(Long publicationId, Long userId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new PublicationNotFoundException());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        if (publication.getHearts().contains(user)) {
            publication.getHearts().remove(user);
        } else {
            publication.getHearts().add(user);
            eventPublisher.publishEvent(new PublicationLikedEvent(this, publication, user));
        }

        publicationRepository.save(publication);
    }

    // task systemo

    public Page<FailedTaskDto> getFailedPlaceTasks(Pageable pageable) {
        return failedPlaceRepository.findAll(pageable)
            .map(task -> modelMapper.map(task, FailedTaskDto.class));
    }

    @Transactional
    public void retryFailedTask(Long taskId) {
        FailedPlaceTask task = failedPlaceRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        eventPublisher.publishEvent(new PublicationCreatedEvent(
                this,
                task.getPublicationId(),
                task.getOsmId(),
                task.getOsmType(),
                1
        ));
        failedPlaceRepository.delete(task);
    }

    public void deleteFailedTask(Long taskId) {
        failedPlaceRepository.deleteById(taskId);
    }
}