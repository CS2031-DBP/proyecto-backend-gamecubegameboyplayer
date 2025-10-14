package com.artpond.backend.publication.domain;

import com.artpond.backend.publication.dto.CreatePublicationDto;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.publication.infrastructure.PublicationRepository;
import com.artpond.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final ModelMapper modelMapper;

    public PublicationResponseDto createPublication(User user, CreatePublicationDto dto) {
            Publication publication = new Publication();
            publication.setDescrption(dto.getDescription());
            publication.setOwner(user);
            publicationRepository.save(publication);
        return modelMapper.map(dto, PublicationResponseDto.class);
    }

    public List<PublicationResponseDto> getAllPublications() {
        List<Publication> publications = publicationRepository.findAll();
        return null;
    }
}