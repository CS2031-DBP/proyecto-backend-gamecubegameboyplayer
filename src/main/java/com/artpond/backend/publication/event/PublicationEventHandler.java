package com.artpond.backend.publication.event;

import com.artpond.backend.map.domain.MapService;
import com.artpond.backend.map.domain.Place;
import com.artpond.backend.map.exception.InvalidPlaceException;
import com.artpond.backend.publication.domain.AiDetectionService;
import com.artpond.backend.publication.domain.FailedAiTask;
import com.artpond.backend.publication.domain.FailedPlaceTask;
import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.publication.exception.PublicationNotFoundException;
import com.artpond.backend.publication.infrastructure.FailedAiRepository;
import com.artpond.backend.publication.infrastructure.FailedPlaceRepository;
import com.artpond.backend.publication.infrastructure.PublicationRepository;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PublicationEventHandler {
    private final MapService mapService;
    private final AiDetectionService aiDetectionService;
    private final PublicationRepository publicationRepository;
    private final FailedPlaceRepository failedPlaceRepository;
    private final FailedAiRepository failedAiRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    private final Bucket nominatimRateLimiter = Bucket.builder()
        .addLimit(limit -> limit.capacity(1).refillIntervally(1, Duration.ofSeconds(1)))
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
                .orElseThrow(() -> new PublicationNotFoundException());
            
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

    @Async("placeProcessingExecutor")
    @TransactionalEventListener
    public void handleAiAnalysis(AiAnalysisRequestedEvent event) {
        log.info("Starting async AI research for publication {}", event.getPublicationId());

        try {
            Publication publication = publicationRepository.findById(event.getPublicationId())
                    .orElseThrow(() -> new PublicationNotFoundException());

            if (publication.getImages().isEmpty()) return;
            
            String imageKey = publication.getImages().get(0).getCleanFileKey();

            boolean isAi = aiDetectionService.analyzeImage(
                event.getPublicationId(),
                event.getUserId(),
                imageKey, 
                event.getPubType()
            );

            if (isAi) {
                log.info("publication {} did not pick up the pencil :C", event.getPublicationId());
                publication.setMachineGenerated(true);
                publicationRepository.save(publication);
            } else {
                log.info("publication {} picked up the pencil :D", event.getPublicationId());
            }

        } catch (Exception e) {
            log.error("Async AI Analysis failed", e);
            FailedAiTask failure = new FailedAiTask();
            failure.setPublicationId(event.getPublicationId());
            failure.setPubType(event.getPubType());
            failure.setUserId(event.getUserId());
            failure.setErrorMessage(e.getMessage() != null ? e.getMessage() : "Unknown error");
            failure.setFailedAt(LocalDateTime.now());
            
            failedAiRepository.save(failure);
        }
    }
}