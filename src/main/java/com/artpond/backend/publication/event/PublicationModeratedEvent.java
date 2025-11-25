package com.artpond.backend.publication.event;

import com.artpond.backend.user.domain.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PublicationModeratedEvent extends ApplicationEvent {
    private final User author;
    private final String postTitle;
    private final String reason;

    public PublicationModeratedEvent(Object source, User author, String postTitle, String reason) {
        super(source);
        this.author = author;
        this.postTitle = postTitle;
        this.reason = reason;
    }
}
