package com.artpond.backend.report.infrastructure;

import com.artpond.backend.report.domain.Report;
import com.artpond.backend.report.domain.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);
    Long countByPublication_IdAndStatus(Long publicationId, ReportStatus status);
}
