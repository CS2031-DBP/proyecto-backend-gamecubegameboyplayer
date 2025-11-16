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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.artpond.backend.map.dto.JsonPlaceDataDto;
import com.artpond.backend.map.dto.PlaceDataDto;
import com.artpond.backend.map.dto.PlaceMapSummaryDto;
import com.artpond.backend.map.exception.InvalidPlaceException;
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
    public Place findOrCreatePlace(Long osmId, String osmType) {
        return mapRepository.findByOsmId(osmId)
            .orElseGet(() -> {
                PlaceDataDto trustedData = fetchPlaceDetailsFromNominatim(osmId, osmType);
                
                Point coordinates = geometryFactory.createPoint(
                    new Coordinate(trustedData.getLongitude(), trustedData.getLatitude())
                );
                coordinates.setSRID(4326);

                Place newPlace = new Place();
                newPlace.setOsmId(trustedData.getOsmId());
                newPlace.setName(trustedData.getName());
                newPlace.setAddress(trustedData.getAddress());
                newPlace.setCoordinates(coordinates);
                newPlace.setPostCount(0L);

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
                .newInstance()
                .scheme("https")
                .host("nominatim.openstreetmap.org")
                .path("/search")
                .queryParam("q", query)
                .queryParam("format", "jsonv2")
                .queryParam("featuretype", "venue")
                .toUriString();

        HttpHeaders headers = createNominatimHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<JsonPlaceDataDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<JsonPlaceDataDto>>() {}
            );
            List<JsonPlaceDataDto> jsonPlaces = response.getBody();
            if (jsonPlaces == null) {
                return List.of();
            }

            return jsonPlaces.stream()
                    .map(this::mapToPlaceDataDto)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar lugares: " + e.getMessage(), e);
        }
    }

    private PlaceDataDto fetchPlaceDetailsFromNominatim(Long osmId, String osmType) {
        String osmIdWithType = osmType.toUpperCase() + osmId;

        String url = UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host("nominatim.openstreetmap.org")
                .path("/lookup")
                .queryParam("osm_ids", osmIdWithType)
                .queryParam("format", "jsonv2")
                .toUriString();

        HttpHeaders headers = createNominatimHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<JsonPlaceDataDto>> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<JsonPlaceDataDto>>() {}
            );
        } catch (HttpClientErrorException e) {
            throw new InvalidPlaceException("Place failed to validate: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Normatim network error: " + e.getMessage(), e);
        }

        if (response.getBody() == null) {
            throw new InvalidPlaceException("Response null for id: " + osmIdWithType);
        } else if (response.getBody().isEmpty()) {
            throw new InvalidPlaceException("Response empty for id: " + osmIdWithType);
        }

        JsonPlaceDataDto nominatimData = response.getBody().get(0);
        return mapToPlaceDataDto(nominatimData);
    }

    private HttpHeaders createNominatimHeaders() {
        HttpHeaders headers = new HttpHeaders();
        // Nominatim requiere un User-Agent único.
        headers.set("User-Agent", "Artpond-Backend/1.0 (contact@artpond.com)");
        return headers;
    }

    private PlaceDataDto mapToPlaceDataDto(JsonPlaceDataDto nominatimData) {
        PlaceDataDto trustedData = new PlaceDataDto();
        trustedData.setOsmId(nominatimData.getOsmId());
        
        // Usar 'name' si existe, si no, usar 'display_name' como fallback
        trustedData.setName(nominatimData.getName() != null ? nominatimData.getName() : nominatimData.getDisplayName());
        
        trustedData.setAddress(nominatimData.getDisplayName());
        trustedData.setLatitude(Double.parseDouble(nominatimData.getLat()));
        trustedData.setLongitude(Double.parseDouble(nominatimData.getLon()));
        return trustedData;
    }
}
