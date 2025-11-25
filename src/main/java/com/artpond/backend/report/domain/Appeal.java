package com.artpond.backend.report.domain;

import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "appeals")
@Data
public class Appeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id", nullable = false)
    private Publication publication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(length = 1000)
    private String justification; // Por qué dice el autor que es humano (link a timelapse, etc.)

    @Enumerated(EnumType.STRING)
    private AppealStatus status = AppealStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;

    private String adminNotes;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
