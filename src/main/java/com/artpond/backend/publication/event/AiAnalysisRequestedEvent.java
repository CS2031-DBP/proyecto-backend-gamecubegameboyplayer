package com.artpond.backend.publication.event;

import com.artpond.backend.publication.domain.PubType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AiAnalysisRequestedEvent extends ApplicationEvent {
    private final Long publicationId;
    private final Long userId;
    private final String username;
    private final PubType pubType;

    public AiAnalysisRequestedEvent(Object source, Long publicationId, Long userId, String username, PubType pubType) {
        super(source);
        this.publicationId = publicationId;
        this.userId = userId;
        this.username = username;
        this.pubType = pubType;
    }
}
