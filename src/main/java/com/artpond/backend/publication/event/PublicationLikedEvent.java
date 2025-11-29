package com.artpond.backend.publication.event;

import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.user.domain.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PublicationLikedEvent extends ApplicationEvent {
    private final Publication publication;
    private final User actor;

    public PublicationLikedEvent(Object source, Publication publication, User actor) {
        super(source);
        this.publication = publication;
        this.actor = actor;
    }
}   