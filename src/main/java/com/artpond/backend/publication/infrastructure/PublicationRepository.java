package com.artpond.backend.publication.infrastructure;

import com.artpond.backend.publication.domain.PubType;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.tag.domain.Tag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.artpond.backend.user.domain.User;


@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByTagsContaining(Tag tag, Pageable pageable);
    
    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByTagsContainingAndContentWarningFalse(Tag tag, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByPlace_IdOrderByCreationDate(Long placeId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByPlace_IdAndContentWarningFalseOrderByCreationDate(Long placeId, Pageable pageable);

    Long countByPlace_IdAndModeratedIsFalse(Long placeId);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByContentWarningFalse(Pageable pageable);
    
    @EntityGraph(attributePaths = {"author", "place"})
    @Override
    Page<Publication> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByPubType(PubType pubType, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByPubTypeAndContentWarningFalse(PubType pubType, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByAuthor(User author, Pageable pageable);
    
    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByAuthorAndPubType(User author, PubType pubType, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByAuthorAndContentWarningFalse(User author, Pageable pageable);
    
    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByAuthorAndPubTypeAndContentWarningFalse(User author, PubType pubType, Pageable pageable);

    @Query("SELECT p FROM User u JOIN u.savedPublications p WHERE u.userId = :userId")
    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findSavedPublicationsByUser(@Param("userId") Long userId, Pageable pageable);

    // Feed principal
    @Query("""
        SELECT p FROM Publication p 
        WHERE p.author.userId IN (
            SELECT f.userId FROM User u JOIN u.following f WHERE u.userId = :userId
        )
        AND (:showExplicit = true OR p.contentWarning = false)
        ORDER BY p.creationDate DESC
    """)
    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findFeedByUserId(
        @Param("userId") Long userId, 
        @Param("showExplicit") boolean showExplicit, 
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"images", "tags", "author", "place"})
    Optional<Publication> findWithDetailsById(Long id);

    @Query("SELECT p.id FROM Publication p JOIN p.hearts h WHERE h.userId = :userId AND p.id IN :pubIds")
    Set<Long> findLikedPublicationIdsByUser(@Param("userId") Long userId, @Param("pubIds") List<Long> pubIds);

    @Query("SELECT p.id FROM User u JOIN u.savedPublications p WHERE u.userId = :userId AND p.id IN :pubIds")
    Set<Long> findSavedPublicationIdsByUser(@Param("userId") Long userId, @Param("pubIds") List<Long> pubIds);

    @Query("SELECT p.id as publicationId, COUNT(h) as count FROM Publication p JOIN p.hearts h WHERE p.id IN :pubIds GROUP BY p.id")
    List<HeartCountProjection> countHeartsByPublicationIds(@Param("pubIds") List<Long> pubIds);
}