package com.artpond.backend.image.domain;

import com.artpond.backend.publication.domain.Publication;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "images")
@Data
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id", nullable = false)
    @JsonBackReference
    private Publication publication;

    @Column(nullable = false)
    private String url;
    
    @Column(nullable = false)
    private String cleanFileKey;
    @Column(nullable = false)
    private String publicFileKey;
}
