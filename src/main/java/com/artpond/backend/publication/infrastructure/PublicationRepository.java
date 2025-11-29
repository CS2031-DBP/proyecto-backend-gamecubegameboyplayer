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
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.artpond.backend.user.domain.User;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByTagsContainingAndModeratedFalse(Tag tag, Pageable pageable);
    
    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByTagsContainingAndContentWarningFalseAndModeratedFalse(Tag tag, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByPlace_IdAndModeratedFalseOrderByCreationDate(Long placeId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByPlace_IdAndContentWarningFalseAndModeratedFalseOrderByCreationDate(Long placeId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByModeratedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByContentWarningFalseAndModeratedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByPubTypeAndModeratedFalse(PubType pubType, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByPubTypeAndContentWarningFalseAndModeratedFalse(PubType pubType, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByAuthorAndModeratedFalse(User author, Pageable pageable);
    
    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByAuthorAndPubTypeAndModeratedFalse(User author, PubType pubType, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByAuthorAndContentWarningFalseAndModeratedFalse(User author, Pageable pageable);
    
    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByAuthorAndPubTypeAndContentWarningFalseAndModeratedFalse(User author, PubType pubType, Pageable pageable);

    // private entities

    Long countByPlace_IdAndModeratedIsFalse(Long placeId);
    
    @EntityGraph(attributePaths = {"author", "place"})
    @Override
    @NonNull Page<Publication> findAll(@NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByModeratedTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findByPubTypeAndModeratedTrue(PubType pubType, Pageable pageable);

    @Query("SELECT p FROM User u JOIN u.savedPublications p WHERE u.userId = :userId AND p.moderated = false")
    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findSavedPublicationsByUser(@Param("userId") Long userId, Pageable pageable);
    

    @Query("""
        SELECT DISTINCT p FROM Publication p 
        JOIN p.author a
        WHERE a IN (
            SELECT f FROM User u JOIN u.following f WHERE u.userId = :userId
        )
        AND (:showExplicit = true OR p.contentWarning = false)
        AND p.moderated = false
        ORDER BY p.creationDate DESC
    """)
    @EntityGraph(attributePaths = {"author", "place"})
    Page<Publication> findFeedByUserId(
        @Param("userId") Long userId, 
        @Param("showExplicit") boolean showExplicit, 
        Pageable pageable
    );

    @Query("""
        SELECT p.id FROM Publication p 
        JOIN p.hearts h 
        WHERE h.userId = :userId AND p.id IN :pubIds
    """)
    Set<Long> findLikedPublicationIdsByUser(
        @Param("userId") Long userId, 
        @Param("pubIds") List<Long> pubIds
    );

    @Query("""
        SELECT p.id FROM User u 
        JOIN u.savedPublications p 
        WHERE u.userId = :userId AND p.id IN :pubIds
    """)
    Set<Long> findSavedPublicationIdsByUser(
        @Param("userId") Long userId, 
        @Param("pubIds") List<Long> pubIds
    );

    @Query("""
        SELECT p.id as publicationId, COUNT(h) as count 
        FROM Publication p 
        LEFT JOIN p.hearts h 
        WHERE p.id IN :pubIds 
        GROUP BY p.id
    """)
    List<HeartCountProjection> countHeartsByPublicationIds(@Param("pubIds") List<Long> pubIds);

    @Query("""
        SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END 
        FROM Publication p 
        JOIN p.hearts h 
        WHERE p.id = :pubId AND h.userId = :userId
    """)
    boolean existsHeart(@Param("pubId") Long pubId, @Param("userId") Long userId);

    @Query("""
        SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END 
        FROM User u 
        JOIN u.savedPublications p 
        WHERE u.userId = :userId AND p.id = :pubId
    """)
    boolean existsSaved(@Param("userId") Long userId, @Param("pubId") Long pubId);

    @Query("SELECT COUNT(h) FROM Publication p JOIN p.hearts h WHERE p.id = :pubId")
    Long countHearts(@Param("pubId") Long pubId);

    Optional<Publication> findWithDetailsById(Long id);
}