package com.artpond.backend.report.domain;

import com.artpond.backend.definitions.exception.BadRequestException;
import com.artpond.backend.definitions.exception.ForbiddenException;
import com.artpond.backend.definitions.exception.NotFoundException;
import com.artpond.backend.report.dto.AppealDto;
import com.artpond.backend.report.infrastructure.AppealRepository;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.infrastructure.PublicationRepository;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppealService {

    private final ModelMapper modelMapper;

    private final AppealRepository appealRepository;
    private final PublicationRepository publicationRepository;
    private final UserRepository userRepository;

    public void createAppeal(Long publicationId, String justification, Long userId) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new NotFoundException("Publicación no encontrada"));

        if (!publication.getAuthor().getUserId().equals(userId)) {
            throw new ForbiddenException("Solo el autor puede apelar.");
        }

        if (publication.getMachineGenerated() != true) {
            throw new BadRequestException("Esta publicación no está marcada como IA, no es necesario apelar.");
        }

        if (appealRepository.existsByPublication_IdAndStatus(publicationId, AppealStatus.PENDING)) {
            throw new BadRequestException("Ya hay una apelación pendiente para esta obra.");
        }

        Appeal appeal = new Appeal();
        appeal.setPublication(publication);
        appeal.setAuthor(publication.getAuthor());
        appeal.setJustification(justification);
        
        appealRepository.save(appeal);
    }

    public Page<AppealDto> getPendingAppeals(Pageable pageable) {
        return appealRepository.findByStatusOrderByCreatedAtDesc(AppealStatus.PENDING, pageable).map(m -> modelMapper.map(m, AppealDto.class));
    }

    @Transactional
    public void resolveAppeal(Long appealId, AppealStatus newStatus, String adminNotes, Long adminId) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new NotFoundException("Apelación no encontrada"));
        
        User admin = userRepository.findById(adminId).orElseThrow();

        if (newStatus == AppealStatus.PENDING) throw new BadRequestException("Debes Aprobar o Rechazar.");

        appeal.setStatus(newStatus);
        appeal.setAdminNotes(adminNotes);
        appeal.setResolvedBy(admin);

        if (newStatus == AppealStatus.APPROVED) {
            Publication pub = appeal.getPublication();
            pub.setMachineGenerated(false);
            publicationRepository.save(pub);
        }

        appealRepository.save(appeal);
    }
}
