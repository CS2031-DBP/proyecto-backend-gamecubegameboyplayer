package com.artpond.backend.report.infrastructure;

import com.artpond.backend.report.domain.Appeal;
import com.artpond.backend.report.domain.AppealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppealRepository extends JpaRepository<Appeal, Long> {
    Page<Appeal> findByStatusOrderByCreatedAtDesc(AppealStatus status, Pageable pageable);
    Boolean existsByPublication_IdAndStatus(Long publicationId, AppealStatus status);
}
