package com.artpond.backend.report.domain;

import com.artpond.backend.definitions.exception.BadRequestException;
import com.artpond.backend.definitions.exception.NotFoundException;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.exception.PublicationNotFoundException;
import com.artpond.backend.publication.infrastructure.PublicationRepository;
import com.artpond.backend.report.dto.CreateReportDto;
import com.artpond.backend.report.dto.ReportDto;
import com.artpond.backend.report.infrastructure.ReportRepository;
import com.artpond.backend.user.domain.User;
import com.artpond.backend.user.exception.UserNotFoundException;
import com.artpond.backend.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ModelMapper modelMapper;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PublicationRepository publicationRepository;

    public void createReport(CreateReportDto dto, Long reporterId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new UserNotFoundException());

        if (dto.getPublicationId() == null && dto.getReportedUserId() == null) {
            throw new BadRequestException("Must report either a publication or a user.");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setReason(dto.getReason());
        report.setDetails(dto.getDetails());

        if (dto.getPublicationId() != null) {
            Publication pub = publicationRepository.findById(dto.getPublicationId())
                    .orElseThrow(() -> new PublicationNotFoundException());
            report.setPublication(pub);
        } else {
            User reportedUser = userRepository.findById(dto.getReportedUserId())
                    .orElseThrow(() -> new UserNotFoundException());
            if (reportedUser.getUserId().equals(reporterId)) {
                 throw new BadRequestException("No puedes reportarte a ti mismo :P");
            }
            report.setReportedUser(reportedUser);
        }

        reportRepository.save(report);
    }

    public Page<ReportDto> getReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map(m -> modelMapper.map(m, ReportDto.class));
    }

    @Transactional
    public void resolveReport(Long reportId, ReportStatus newStatus, String notes, Long moderatorId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found"));
        
        User moderator = userRepository.findById(moderatorId).orElseThrow();

        report.setStatus(newStatus);
        report.setResolutionNotes(notes);
        report.setResolvedBy(moderator);
        
        reportRepository.save(report);
    }
}
