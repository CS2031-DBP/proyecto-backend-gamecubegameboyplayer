package com.artpond.backend.publication.event;

import com.artpond.backend.publication.domain.MediaType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AiAnalysisRequestedEvent extends ApplicationEvent {
    private final Long publicationId;
    private final MediaType mediaType;

    public AiAnalysisRequestedEvent(Object source, Long publicationId, MediaType mediaType) {
        super(source);
        this.publicationId = publicationId;
        this.mediaType = mediaType;
    }
}
