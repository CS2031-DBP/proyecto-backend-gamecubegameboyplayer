package com.artpond.backend.publication.infrastructure;

import com.artpond.backend.publication.domain.PubType;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.tag.domain.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.artpond.backend.user.domain.User;


@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
    Page<Publication> findByTagsContaining(Tag tag, Pageable pageable);
    Page<Publication> findByPlace_IdOrderByCreationDate(Long placeId, Pageable pageable);
    
    Page<Publication> findByContentWarningFalse(Pageable pageable); 
    
    Page<Publication> findByTagsContainingAndContentWarningFalse(Tag tag, Pageable pageable);
    
    Page<Publication> findByPlace_IdAndContentWarningFalseOrderByCreationDate(Long placeId, Pageable pageable);

    Long countByPlace_IdAndModeratedIsFalse(Long placeId);

    Page<Publication> findByPubType(PubType pubType, Pageable pageable);

    Page<Publication> findByPubTypeAndContentWarningFalse(PubType pubType, Pageable pageable);

    Page<Publication> findByPubTypeNot(PubType pubType, Pageable pageable);
    Page<Publication> findByPubTypeNotAndContentWarningFalse(PubType pubType, Pageable pageable);

    Page<Publication> findByAuthor(User author, Pageable pageable);
    Page<Publication> findByAuthorAndPubType(User author, PubType pubType, Pageable pageable);

    Page<Publication> findByAuthorAndContentWarningFalse(User author, Pageable pageable);
    Page<Publication> findByAuthorAndPubTypeAndContentWarningFalse(User author, PubType pubType, Pageable pageable);

    @Query("SELECT p FROM User u JOIN u.savedPublications p WHERE u.userId = :userId")
    Page<Publication> findSavedPublicationsByUser(@Param("userId") Long userId, Pageable pageable);
}