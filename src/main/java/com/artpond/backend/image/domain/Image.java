package com.artpond.backend.image.domain;

import com.artpond.backend.publication.domain.Publication;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
//table???
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String url;
    private String altText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id")
    private Publication publication;
}
