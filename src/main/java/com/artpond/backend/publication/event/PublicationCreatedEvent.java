package com.artpond.backend.publication.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PublicationCreatedEvent extends ApplicationEvent {
    private final Long publicationId;
    private final Long osmId;
    private final String osmType;
    private final int attemptNumber;

    public PublicationCreatedEvent(Object source, Long publicationId, Long osmId, String osmType) {
        this(source, publicationId, osmId, osmType, 1);
    }
    
    public PublicationCreatedEvent(Object source, Long publicationId, Long osmId, String osmType, int attemptNumber) {
        super(source);
        this.publicationId = publicationId;
        this.osmId = osmId;
        this.osmType = osmType;
        this.attemptNumber = attemptNumber;
    }
}