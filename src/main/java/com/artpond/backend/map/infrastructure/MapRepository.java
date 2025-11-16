package com.artpond.backend.map.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.artpond.backend.map.domain.Place;
import com.artpond.backend.map.dto.PlaceMapSummaryDto;

@Repository
public interface MapRepository extends JpaRepository<Place, Long> {
    @Query(value = """
        SELECT 
            p.id as id, 
            p.name as name, 
            ST_Y(p.coordinates::geometry) as latitude,
            ST_X(p.coordinates::geometry) as longitude,
            p.post_count as postCount
        FROM places p
        WHERE p.coordinates && ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326)
        AND p.post_count > 0
        """, nativeQuery = true)
    List<PlaceMapSummaryDto> findPlacesInBoundingBox(
        @Param("minLat") double minLat,
        @Param("minLon") double minLon,
        @Param("maxLat") double maxLat,
        @Param("maxLon") double maxLon
    );

    Optional<Place> findByOsmId(Long osmId);
}