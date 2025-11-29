package com.artpond.backend.publication.infrastructure;

import com.artpond.backend.publication.domain.FailedAiTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedAiRepository extends JpaRepository<FailedAiTask, Long> {
}
