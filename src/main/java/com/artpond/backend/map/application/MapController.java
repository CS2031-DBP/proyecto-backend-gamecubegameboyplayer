package com.artpond.backend.map.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artpond.backend.map.domain.MapService;
import com.artpond.backend.map.dto.PlaceMapSummaryDto;
import com.artpond.backend.map.dto.PlaceDataDto;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.user.domain.User;

import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {
    private final MapService mapService;

    @GetMapping("/in-view")
    public ResponseEntity<List<PlaceMapSummaryDto>> getPlacesInView(
            @RequestParam double minLat,
            @RequestParam double minLon,
            @RequestParam double maxLat,
            @RequestParam double maxLon
    ) {
        List<PlaceMapSummaryDto> places = mapService.getPlacesInView(minLat, minLon, maxLat, maxLon);
        return ResponseEntity.ok(places);
    }

    @GetMapping("/{placeId}/posts")
    public ResponseEntity<Page<PublicationResponseDto>> getPostsForPlace(
            @PathVariable Long placeId,
            Pageable pageable,
            @AuthenticationPrincipal User user) {
                
        Page<PublicationResponseDto> posts = mapService.getPlacePosts(placeId, pageable, user);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlaceDataDto>> searchPlaces(@RequestParam String query) {
        return ResponseEntity.ok(mapService.searchNominatim(query));
    }
}