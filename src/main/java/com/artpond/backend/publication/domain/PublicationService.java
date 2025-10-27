package com.artpond.backend.publication.domain;

import com.artpond.backend.image.domain.Image;
import com.artpond.backend.publication.dto.CreatePublicationDto;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.publication.infrastructure.PublicationRepository;
import com.artpond.backend.tag.domain.Tag;
import com.artpond.backend.tag.infrastructure.TagRepository;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.domain.UserService;
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
    // no me gusta tener que usar user y tag repository solo para crear una publication.
    // deberia de hacer un endopint en tags para recuperar las tags de un fandom como ao3

    public Publication findPublicationById(Long id) {
        return publicationRepository.findById(id).orElseThrow();
    }


    public PublicationResponseDto createPublication(CreatePublicationDto dto, String username) {
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

            // images (if you have any)
            publication.setImages(dto.getImages());


            return modelMapper.map(publicationRepository.save(publication), PublicationResponseDto.class);
    }

    public Page<PublicationResponseDto> getAllPublications(Pageable pageable) {
        return publicationRepository.findAll(pageable).map(pub -> modelMapper.map(pub, PublicationResponseDto.class));
    }

    public PublicationResponseDto getPublicationById(Long id) {
        return modelMapper.map(publicationRepository.findById(id), PublicationResponseDto.class);
    }

    public PublicationResponseDto saveImages(Long id, List<Image> images) {
        Publication publication = publicationRepository.findById(id).orElseThrow();
        publication.setImages(images);
        return modelMapper.map(publicationRepository.save(publication), PublicationResponseDto.class);
    }
}