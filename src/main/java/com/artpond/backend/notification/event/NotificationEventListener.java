package com.artpond.backend.notification.event;

import com.artpond.backend.comment.event.CommentCreatedEvent;
import com.artpond.backend.notification.domain.NotificationService;
import com.artpond.backend.notification.domain.NotificationType;
import com.artpond.backend.publication.event.PublicationLikedEvent;
import com.artpond.backend.publication.event.PublicationModeratedEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleCommentCreated(CommentCreatedEvent event) {
        var comment = event.getComment();
        var author = comment.getUser();
        var publication = comment.getPublication();

        if (comment.getParent() == null) {
            notificationService.createNotification(
                publication.getAuthor(),
                author,
                NotificationType.COMMENT_ON_POST,
                publication.getId(),
                author.getDisplayName() + " comentó en tu publicación."
            );
        } else {
            notificationService.createNotification(
                comment.getParent().getUser(),
                author,
                NotificationType.REPLY_TO_COMMENT,
                publication.getId(),
                author.getDisplayName() + " respondió a tu comentario."
            );
        }
    }

    @Async
    @EventListener
    public void handlePublicationLiked(PublicationLikedEvent event) {
        var pub = event.getPublication();
        var actor = event.getActor();
        
        notificationService.createNotification(
            pub.getAuthor(),
            actor,
            NotificationType.HEART_ON_POST,
            pub.getId(),
            actor.getDisplayName() + " le gustó tu publicación."
        );
    }

    @Async
    @EventListener
    public void handlePublicationModerated(PublicationModeratedEvent event) {
        notificationService.createNotification(
            event.getAuthor(),
            null,
            NotificationType.CONTENT_MODERATED,
            null,
            "Tu publicación '" + event.getPostTitle() + "' ha sido eliminada por un moderador."
        );
    }
}