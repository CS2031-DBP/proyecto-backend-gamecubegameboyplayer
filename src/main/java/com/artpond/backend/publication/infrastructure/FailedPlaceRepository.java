package com.artpond.backend.publication.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artpond.backend.publication.domain.FailedPlaceTask;

@Repository
public interface FailedPlaceRepository extends JpaRepository<FailedPlaceTask, Long> {
    
}
