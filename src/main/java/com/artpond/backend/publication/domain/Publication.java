package com.artpond.backend.publication.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.artpond.backend.comment.domain.Comment;
import com.artpond.backend.image.domain.Image;
import com.artpond.backend.tag.domain.Tag;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.UpdateTimestamp;

import com.artpond.backend.user.domain.User;

import lombok.Data;

@Entity
@Table(name = "publications")
@Data
public class Publication {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonBackReference
    private User author;

    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private Boolean contentWarning = false;

    @Column(nullable = false)
    private Boolean moderated = false;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime creationDate = LocalDateTime.now();

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Image> images = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "publication_tags",
            joinColumns = @JoinColumn(name = "publication_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();
    /*
    @UpdateTimestamp
    @Column(nullable = false)
    private Date updatedDate;
    @ElementCollection
    @OrderBy("tags")
    private List<String> tags = new ArrayList<>();  /// needed but i think it need t be a more complex system

    @OneToMany
    private List<User> likes;

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        return Objects.equals(id, ((Publication)obj).id);
    } /// &&&

    @Override
    public int hashCode() {
        return Objects.hash(id);
    } /// &&&

     */
}
