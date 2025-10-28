package com.artpond.backend.publication.infrastructure;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.tag.domain.Tag;
import com.artpond.backend.user.domain.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
    Page<Publication> findByTagsContaining(Tag tag, Pageable pageable);
}
// should it be transactional