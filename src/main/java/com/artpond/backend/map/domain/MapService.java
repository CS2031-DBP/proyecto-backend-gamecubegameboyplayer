package com.artpond.backend.map.domain;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.artpond.backend.map.dto.PlaceDataDto;
import com.artpond.backend.map.dto.PlaceMapSummaryDto;
import com.artpond.backend.map.infrastructure.MapRepository;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.dto.PublicationResponseDto;
import com.artpond.backend.publication.infrastructure.PublicationRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {
    private final RestTemplate restTemplate;
    private final PublicationRepository publicationRepository;
    private final MapRepository mapRepository;
    private final ModelMapper modelMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public Page<PublicationResponseDto> getPlacePosts(Long placeId, Pageable pageable) {
        Page<Publication> posts = publicationRepository.findByPlace_IdOrderByCreationDate(placeId, pageable);
        return posts.map(publication -> modelMapper.map(publication, PublicationResponseDto.class));
    }

    public List<PlaceMapSummaryDto> getPlacesInView(double minLat, double minLon, double maxLat, double maxLon) {
        return mapRepository.findPlacesInBoundingBox(minLat, minLon, maxLat, maxLon);
    }

    @Transactional
    public Place findOrCreatePlace(PlaceDataDto placeData) {
        return mapRepository.findByOsmId(placeData.getOsmId())
            .orElseGet(() -> {
                Point coordinates = geometryFactory.createPoint(
                    new Coordinate(placeData.getLongitude(), placeData.getLatitude())
                );
                coordinates.setSRID(4326); 
                
                Place newPlace = modelMapper.map(placeData, Place.class);
                newPlace.setCoordinates(coordinates);
                return mapRepository.save(newPlace);
            });
    }

    @Transactional
    public void updatePostCount(Long placeId) {
        if (placeId == null) return;
        
        Long activePosts = publicationRepository.countByPlace_IdAndModeratedIsFalse(placeId);
        
        Place place = mapRepository.findById(placeId)
            .orElseThrow(() -> new RuntimeException("Place not found"));
            
        place.setPostCount(activePosts);
        mapRepository.save(place);
    }

    public List<PlaceDataDto> searchNominatim(String query) {
        String url = UriComponentsBuilder
            .fromUriString("https://nominatim.openstreetmap.org/search")
            .queryParam("q", query)
            .queryParam("format", "jsonv2")
            .queryParam("featuretype", "venue")
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Artpond-Backend/1.0");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<PlaceDataDto>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<List<PlaceDataDto>>() {}
        );
        
        return response.getBody();
    }
}
