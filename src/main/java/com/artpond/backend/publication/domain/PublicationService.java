package com.artpond.backend.publication.domain;

import com.artpond.backend.publication.dto.CreatePublicationDto;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.publication.dto.UpdatePublicationDto;
import com.artpond.backend.publication.infrastructure.PublicationRepository;
import com.artpond.backend.user.domain.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final ModelMapper modelMapper;

    public List<PublicationResponseDto> getAllPublications() {
        List<Publication> publications = publicationRepository.findAll();
        return null;
    }

    public Optional<Publication> getPublicationFromId(Long id) {
        return publicationRepository.findById(id);
    }

    public PublicationResponseDto createPublication(User user, CreatePublicationDto dto) {
        Publication publication = modelMapper.map(dto, Publication.class);
        publication.setOwner(user);
        publicationRepository.save(publication);
        return modelMapper.map(publication, PublicationResponseDto.class);
    }

    public Publication updatePublication(Publication post, UpdatePublicationDto dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePost'");
    }

    
}