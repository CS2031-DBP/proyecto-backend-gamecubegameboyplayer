package com.artpond.backend.publication.event;

import com.artpond.backend.map.domain.MapService;
import com.artpond.backend.map.domain.Place;
import com.artpond.backend.map.exception.InvalidPlaceException;
import com.artpond.backend.publication.domain.FailedPlaceTask;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.infrastructure.FailedPlaceRepository;
import com.artpond.backend.publication.infrastructure.PublicationRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PublicationEventHandler {
    private final MapService mapService;
    private final PublicationRepository publicationRepository;
    private final FailedPlaceRepository failedPlaceRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    private final Bucket nominatimRateLimiter = Bucket.builder()
        .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofSeconds(1))))
        .build();
    
    @Async("placeProcessingExecutor")
    @EventListener
    public void handlePublicationCreated(PublicationCreatedEvent event) {
        int attemptNumber = event.getAttemptNumber();

        if (attemptNumber > 1) {
            try {
                long delaySeconds = (long) Math.pow(2, attemptNumber - 1);
                log.info("Waiting {} seconds before retry attempt {} for publication {}", 
                    delaySeconds, attemptNumber, event.getPublicationId());
                Thread.sleep(delaySeconds * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Backoff sleep interrupted for publication {}", event.getPublicationId());
                return;
            }
        }
        
        log.info("time to update the place of publ num. {} (attempt {}/{})", 
            event.getPublicationId(), attemptNumber, MAX_RETRY_ATTEMPTS);
        
        try {
            nominatimRateLimiter.asBlocking().consume(1);

            Publication publication = publicationRepository.findById(event.getPublicationId())
                .orElseThrow(() -> new RuntimeException("Publication not found: " + event.getPublicationId()));
            
            Place place = mapService.findOrCreatePlace(event.getOsmId(), event.getOsmType());
            
            publication.setPlace(place);
            publicationRepository.save(publication);
            
            mapService.updatePostCount(place.getId());
            log.info("Done. Publication {} is now on place {} ({}) after trying {} time/s", 
                event.getPublicationId(), place.getId(), place.getName(), attemptNumber);
                
        } catch (InvalidPlaceException e) {
            log.error("Invalid place for publication {}: {}. Stop.", 
                event.getPublicationId(), e.getMessage());
                
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Place assign to publication {} was interrupted.", 
                event.getPublicationId(), e);
            retryIfPossible(event, e);
            
        } catch (Exception e) {
            log.error("Failed to process place for publication {} (attempt {}): {}", 
                event.getPublicationId(), attemptNumber, e.getMessage(), e);
            retryIfPossible(event, e);
        }
    }
    
    private void retryIfPossible(PublicationCreatedEvent event, Exception cause) {
        if (event.getAttemptNumber() < MAX_RETRY_ATTEMPTS) {
            int nextAttempt = event.getAttemptNumber() + 1;
            log.info("Retry publication {} for later (attempt count {})", 
                event.getPublicationId(), nextAttempt);
            
            PublicationCreatedEvent retryEvent = new PublicationCreatedEvent(
                this,
                event.getPublicationId(),
                event.getOsmId(),
                event.getOsmType(),
                nextAttempt
            );
            
            eventPublisher.publishEvent(retryEvent);
        } else {
            log.error("Giving up. Tried ({}) time/s for publication {}.", 
                MAX_RETRY_ATTEMPTS, event.getPublicationId());

            FailedPlaceTask failure = new FailedPlaceTask();
            failure.setPublicationId(event.getPublicationId());
            failure.setOsmId(event.getOsmId());
            failure.setOsmType(event.getOsmType());
            failure.setErrorMessage(cause.getMessage());
            failure.setFailedAt(LocalDateTime.now());
            failedPlaceRepository.save(failure);
        }
    }
}