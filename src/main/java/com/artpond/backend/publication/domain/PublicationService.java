package com.artpond.backend.publication.domain;

import com.artpond.backend.definitions.exception.BadRequestException;
import com.artpond.backend.definitions.exception.ForbiddenException;
import com.artpond.backend.definitions.exception.NotFoundException;
import com.artpond.backend.image.domain.Image;
import com.artpond.backend.image.dto.ImageResponseDto;
import com.artpond.backend.image.dto.ImageUploadDto;
import com.artpond.backend.image.domain.ImageService;
import com.artpond.backend.map.dto.PlaceDataDto;
import com.artpond.backend.publication.dto.*;
import com.artpond.backend.publication.event.AiAnalysisRequestedEvent;
import com.artpond.backend.publication.event.PublicationCreatedEvent;
import com.artpond.backend.publication.event.PublicationLikedEvent;
import com.artpond.backend.publication.event.PublicationModeratedEvent;
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
import com.artpond.backend.publication.infrastructure.FailedAiRepository;
import com.artpond.backend.publication.infrastructure.FailedPlaceRepository;
import com.artpond.backend.publication.infrastructure.HeartCountProjection;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class PublicationService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ImageService imageService;
    private final ApplicationEventPublisher eventPublisher;

    private final PublicationRepository publicationRepository;
    private final FailedPlaceRepository failedPlaceRepository;
    private final FailedAiRepository failedAiRepository;

    public Publication findPublicationById(Long id) {
        return publicationRepository.findById(id).orElseThrow(PublicationNotFoundException::new);
    }

    // Método privado genérico para enriquecer páginas de publicaciones de manera eficiente (N+1 solution)
    private Page<PublicationResponseDto> enrichPage(Page<Publication> page, User currentUser) {
        if (page.isEmpty()) {
            return page.map(p -> toDto(p, currentUser, null, null, null));
        }

        List<Long> pubIds = page.getContent().stream().map(Publication::getId).toList();

        // Datos batch por defecto vacíos
        Set<Long> likedIds = Collections.emptySet();
        Set<Long> savedIds = Collections.emptySet();

        // Solo consultamos si hay usuario logueado
        if (currentUser != null) {
            likedIds = publicationRepository.findLikedPublicationIdsByUser(currentUser.getUserId(), pubIds);
            savedIds = publicationRepository.findSavedPublicationIdsByUser(currentUser.getUserId(), pubIds);
        }

        // Contar likes para todos los posts de la página en una sola query
        Map<Long, Long> likeCounts = publicationRepository.countHeartsByPublicationIds(pubIds).stream()
                .collect(Collectors.toMap(HeartCountProjection::getPublicationId, HeartCountProjection::getCount));

        // Variables finales para usar en lambda
        Set<Long> finalLikedIds = likedIds;
        Set<Long> finalSavedIds = savedIds;

        return page.map(pub -> {
            boolean isLiked = finalLikedIds.contains(pub.getId());
            boolean isSaved = finalSavedIds.contains(pub.getId());
            int count = likeCounts.getOrDefault(pub.getId(), 0L).intValue();
            
            return toDto(pub, currentUser, isLiked, isSaved, count);
        });
    }

    private PublicationResponseDto toDto(Publication pub, User viewer, Boolean precalcLiked, Boolean precalcSaved, Integer precalcHeartsCount) {
        PublicationResponseDto dto = new PublicationResponseDto();
        dto.setId(pub.getId());
        dto.setDescription(pub.getDescription());
        dto.setContentWarning(pub.getContentWarning());
        dto.setMachineGenerated(pub.getMachineGenerated());
        dto.setManuallyVerified(pub.getManuallyVerified());
        dto.setModerated(pub.getModerated());
        dto.setCreationDate(pub.getCreationDate());
        dto.setAuthor(modelMapper.map(pub.getAuthor(), PublicUserDto.class));
        dto.setCommentsCount(pub.getCommentsCount());
        dto.setPubType(pub.getPubType().toString());

        if (precalcHeartsCount != null) {
            dto.setHeartsCount(precalcHeartsCount);
            dto.setLikedByMe(precalcLiked != null ? precalcLiked : false);
            dto.setSavedByMe(precalcSaved != null ? precalcSaved : false);
        } else {
            dto.setHeartsCount(pub.getHeartsCount()); 
            if (viewer != null) {
                boolean isLiked = pub.getHearts().stream()
                        .anyMatch(u -> u.getUserId().equals(viewer.getUserId()));
                dto.setLikedByMe(isLiked);
                
                boolean isSaved = viewer.getSavedPublications().stream()
                        .anyMatch(p -> p.getId().equals(pub.getId()));
                dto.setSavedByMe(isSaved);
            } else {
                dto.setLikedByMe(false);
                dto.setSavedByMe(false);
            }
        }

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

        dto.setTags(pub.getTags().stream()
                        .map(tg -> modelMapper.map(tg, TagsResponseDto.class))
                        .collect(toList()));

        dto.setPlace(pub.getPlace() != null ? modelMapper.map(pub.getPlace(), PlaceDataDto.class) : null);

        if (Boolean.TRUE.equals(pub.getModerated())) {
            dto.setDescription("[MODERATED CONTENT]");
            dto.setImages(null);
            dto.setTags(null);
            dto.setPlace(null);
            dto.setCommentsCount(0);
            dto.setHeartsCount(0);
            dto.setModerated(true);
        }

        return dto;
    }

    private PublicationResponseDto toDto(Publication pub, User viewer) {
        return toDto(pub, viewer, null, null, null);
    }

    @Transactional
    public PublicationCreatedDto createPublication(PublicationRequestDto dto, List<MultipartFile> imageFiles,
            String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);

        validatePublicationRequest(dto, imageFiles, user);

        Publication publication = new Publication();
        publication.setDescription(dto.getDescription());
        publication.setAuthor(user);
        publication.setContentWarning(dto.getContentWarning() != null ? dto.getContentWarning() : false);
        publication.setHideCleanImage(dto.getHideCleanImage() != null ? dto.getHideCleanImage() : false);
        publication.setPubType(dto.getPubType());

        if (dto.getMachineGenerated()) {
            publication.setMachineGenerated(true);
            publication.setManuallyVerified(true);
        } else {
            publication.setMachineGenerated(false);
            publication.setManuallyVerified(false);
        }

        if (dto.getPubType() != PubType.TEXT && dto.getTags() != null) {
            List<Tag> tagEntities = dto.getTags().stream()
                    .filter(t -> t != null && !t.trim().isEmpty())
                    .map(tagName -> tagRepository.findByName(tagName)
                            .orElseGet(() -> {
                                Tag newTag = new Tag();
                                newTag.setName(tagName);
                                return tagRepository.save(newTag);
                            }))
                    .collect(toList());
            publication.setTags(tagEntities);
        }

        Publication savedPublication = publicationRepository.save(publication);

        if (dto.getPubType() != PubType.TEXT && imageFiles != null) {
            List<String> uploadedCleanKeys = new ArrayList<>();
            List<String> uploadedPublicKeys = new ArrayList<>();

            try {
                List<ImageUploadDto> uploadResults = imageService.uploadImagesForPublication(
                        imageFiles, username, savedPublication.getId(), user.getRole());

                List<Image> imageEntities = uploadResults.stream()
                        .map(imgResult -> {
                            uploadedCleanKeys.add(imgResult.getCleanFileKey());
                            uploadedPublicKeys.add(imgResult.getPublicFileKey());

                            Image img = new Image();
                            img.setUrl(imgResult.getPublicUrl());
                            img.setCleanFileKey(imgResult.getCleanFileKey());
                            img.setPublicFileKey(imgResult.getPublicFileKey());
                            img.setPublication(savedPublication);
                            return img;
                        })
                        .collect(toList());

                savedPublication.setImages(imageEntities);
                Publication finalPublication = publicationRepository.save(savedPublication);
                publishPostCreationEvents(dto, finalPublication);
            } catch (Exception e) {
                if (!uploadedCleanKeys.isEmpty()) {
                    imageService.deleteMultipleImages(uploadedCleanKeys, uploadedPublicKeys);
                }
                throw new PublicationCreationException("Error procesando imágenes: " + e.getMessage());
            }
        }

        return modelMapper.map(savedPublication, PublicationCreatedDto.class);
    }

    private void validatePublicationRequest(PublicationRequestDto dto, List<MultipartFile> imageFiles, User user) {
        if (dto.getContentWarning() == true && user.getShowExplicit() != true) {
            throw new PublicationCreationException("No puedes crear un post explicito sin el permiso correspondiente.");
        }
        
        if (dto.getPubType() == PubType.TEXT) {
            if (imageFiles != null && !imageFiles.isEmpty()) {
                throw new PublicationCreationException("Los posts de comunidad (TEXT) no pueden llevar imágenes.");
            }
            if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
                throw new PublicationCreationException("La descripción no puede estar vacía en un post de texto.");
            }
        } else {
            if (imageFiles == null || imageFiles.isEmpty()) {
                throw new PublicationCreationException("Se requiere al menos una imagen.");
            }
            int maxImagesAllowed = (user.getRole() == Role.ARTIST) ? ImageService.MAX_IMAGES_ARTIST
                    : ImageService.MAX_IMAGES_USER;
            if (imageFiles.size() > maxImagesAllowed) {
                throw new PublicationCreationException(
                        "Tu rol solo permite subir hasta " + maxImagesAllowed + " imágenes.");
            }
            if ((dto.getOsmId() != null && dto.getOsmType() == null)
                    || (dto.getOsmId() == null && dto.getOsmType() != null)) {
                throw new PublicationCreationException("osmId y osmType deben proporcionarse juntos.");
            }
        }
    }

    private void publishPostCreationEvents(PublicationRequestDto dto, Publication savedPublication) {
        if (dto.getPubType() != PubType.TEXT && dto.getOsmId() != null && dto.getOsmType() != null) {
            eventPublisher.publishEvent(
                    new PublicationCreatedEvent(this, savedPublication.getId(), dto.getOsmId(), dto.getOsmType()));
        }
        if (dto.getMachineGenerated() != true && dto.getPubType() != PubType.TEXT) {
            eventPublisher.publishEvent(new AiAnalysisRequestedEvent(this, savedPublication.getId(), savedPublication.getAuthor().getUserId(), dto.getPubType()));
        }
    }

    public Page<PublicationResponseDto> getAllPublicationsForAdmin(Pageable pageable, PubType pubType) {
        Page<Publication> page;
        
        if (pubType != null) {
             page = publicationRepository.findByPubTypeAndModeratedTrue(pubType, pageable);
        } else {
             page = publicationRepository.findByModeratedTrue(pageable);
        }

        return enrichPage(page, null); 
    }

    public Page<PublicationResponseDto> getAllPublications(Pageable pageable, User currentUser, PubType pubType) {
        boolean canSeeExplicit = canSeeExplicitContent(currentUser);
        Page<Publication> page;

        if (pubType != null) {
            page = canSeeExplicit 
                    ? publicationRepository.findByPubTypeAndModeratedFalse(pubType, pageable)
                    : publicationRepository.findByPubTypeAndContentWarningFalseAndModeratedFalse(pubType, pageable);
        } else {
            page = canSeeExplicit 
                    ? publicationRepository.findByModeratedFalse(pageable)
                    : publicationRepository.findByContentWarningFalseAndModeratedFalse(pageable);
        }

        return enrichPage(page, currentUser);
    }

    public Page<PublicationResponseDto> getUserPublications(Pageable pageable, User currentUser, Long id, PubType pubType) {
        boolean canSeeExplicit = canSeeExplicitContent(currentUser);
        User author = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException());
        Page<Publication> page;

        if (pubType != null) {
            page = canSeeExplicit 
                    ? publicationRepository.findByAuthorAndPubTypeAndModeratedFalse(author, pubType, pageable)
                    : publicationRepository.findByAuthorAndPubTypeAndContentWarningFalseAndModeratedFalse(author, pubType, pageable);
        } else {
            page = canSeeExplicit 
                    ? publicationRepository.findByAuthorAndModeratedFalse(author, pageable)
                    : publicationRepository.findByAuthorAndContentWarningFalseAndModeratedFalse(author, pageable);
        }

        return enrichPage(page, currentUser);
    }

    public Page<PublicationResponseDto> getPublicationsByTag(String tagName, Pageable pageable, User currentUser) {
        Tag tag = tagRepository.findByName(tagName).orElseThrow(() -> new NotFoundException("Tag no existe"));
        boolean canSeeExplicit = canSeeExplicitContent(currentUser);
        Page<Publication> page = canSeeExplicit ? publicationRepository.findByTagsContainingAndModeratedFalse(tag, pageable)
                : publicationRepository.findByTagsContainingAndContentWarningFalseAndModeratedFalse(tag, pageable);
        return enrichPage(page, currentUser);
    }

    private boolean canSeeExplicitContent(User user) {
        return user != null && Boolean.TRUE.equals(user.getShowExplicit());
    }

    @Transactional(readOnly = true)
    public PublicationResponseDto getPublicationById(Long id, User currentUser) {
        Publication publication = publicationRepository.findWithDetailsById(id)
                .orElseThrow(PublicationNotFoundException::new);

        if (Boolean.TRUE.equals(publication.getContentWarning())) {
            if (currentUser == null)
                throw new ForbiddenException("Inicia sesión para ver contenido explícito.");
            if (!Boolean.TRUE.equals(currentUser.getShowExplicit()))
                throw new ForbiddenException("Contenido explícito desactivado en tu perfil.");
        }

        Boolean isLiked = false;
        Boolean isSaved = false;
        
        int heartsCount = publicationRepository.countHearts(id).intValue(); 
        
        if (currentUser != null) {
            isLiked = publicationRepository.existsHeart(id, currentUser.getUserId());
            isSaved = publicationRepository.existsSaved(currentUser.getUserId(), id);
        }
        
        return toDto(publication, currentUser, isLiked, isSaved, heartsCount);
    }

    @Transactional
    public PublicationResponseDto patchPublication(Long id, UpdatePublicationDto dto, Long userId) {
        Publication pub = publicationRepository.findById(id).orElseThrow(PublicationNotFoundException::new);

        if (!pub.getAuthor().getUserId().equals(userId))
            throw new ForbiddenException("No puedes editar una publicación que no te pertenece.");

        if (dto.getDescription() != null)
            pub.setDescription(dto.getDescription());
        if (dto.getContentWarning() != null)
            pub.setContentWarning(dto.getContentWarning());

        if (dto.getTags() != null) {
            List<Tag> newTags = dto.getTags().stream()
                    .filter(t -> t != null && !t.trim().isEmpty())
                    .map(tagName -> tagRepository.findByName(tagName)
                            .orElseGet(() -> {
                                Tag newTag = new Tag();
                                newTag.setName(tagName);
                                return tagRepository.save(newTag);
                            }))
                    .collect(toList());
            pub.setTags(newTags);
        }

        return toDto(publicationRepository.save(pub), pub.getAuthor());
    }

    @Transactional
    public Void deletePublicationById(Long id, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Publication publication = publicationRepository.findById(id).orElseThrow(PublicationNotFoundException::new);

        boolean isOwner = publication.getAuthor().getUserId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN || user.getRole() == Role.MODERATOR;

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("No tienes permiso para eliminar esta publicación.");
        }

        if (isAdmin && !isOwner) {
            String preview = publication.getDescription() != null
                    ? publication.getDescription().substring(0, Math.min(publication.getDescription().length(), 30))
                    : "Publicación #" + publication.getId();
            eventPublisher.publishEvent(
                    new PublicationModeratedEvent(this, publication.getAuthor(), preview, "Eliminado por moderación."));
        }

        List<String> cleanKeys = publication.getImages().stream().map(Image::getCleanFileKey).collect(toList());
        List<String> publicKeys = publication.getImages().stream().map(Image::getPublicFileKey).collect(toList());
        imageService.deleteMultipleImages(cleanKeys, publicKeys);

        publicationRepository.deleteById(id);
        return null;
    }

    @Transactional
    public void toggleHeart(Long publicationId, Long userId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(PublicationNotFoundException::new);
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (publication.getHearts().contains(user)) {
            publication.getHearts().remove(user);
        } else {
            publication.getHearts().add(user);
            eventPublisher.publishEvent(new PublicationLikedEvent(this, publication, user));
        }
        publicationRepository.save(publication);
    }

    // tasks
    public Page<FailedPlaceTaskDto> getFailedPlaceTasks(Pageable pageable) {
        return failedPlaceRepository.findAll(pageable).map(task -> modelMapper.map(task, FailedPlaceTaskDto.class));
    }

    @Transactional
    public void retryFailedTask(Long taskId) {
        FailedPlaceTask task = failedPlaceRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        eventPublisher.publishEvent(
                new PublicationCreatedEvent(this, task.getPublicationId(), task.getOsmId(), task.getOsmType(), 1));
        failedPlaceRepository.delete(task);
    }

    public void deleteFailedTask(Long taskId) {
        failedPlaceRepository.deleteById(taskId);
    }

    public Page<FailedAiTaskDto> getFailedAiTasks(Pageable pageable) {
        return failedAiRepository.findAll(pageable).map(task -> modelMapper.map(task, FailedAiTaskDto.class));
    }

    @Transactional
    public void retryFailedAiTask(Long taskId) {
        FailedAiTask task = failedAiRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("AI Task not found"));
        eventPublisher.publishEvent(new AiAnalysisRequestedEvent(this, task.getPublicationId(), task.getUserId(), task.getPubType()));
        failedAiRepository.delete(task);
    }

    public void deleteFailedAiTask(Long taskId) {
        failedAiRepository.deleteById(taskId);
    }

    // following system
    public Page<PublicationResponseDto> getFeed(Pageable pageable, User currentUser) {
        Page<Publication> page = publicationRepository.findFeedByUserId(
            currentUser.getUserId(), 
            currentUser.getShowExplicit(), 
            pageable
        );
        return enrichPage(page, currentUser);
    }
}