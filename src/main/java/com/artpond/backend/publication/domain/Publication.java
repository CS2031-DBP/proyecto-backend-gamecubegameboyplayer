package com.artpond.backend.publication.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.artpond.backend.publication.domain.hearts.Heart;
import com.artpond.backend.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Data;

@Data
@Entity
public class Publication {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User owner;

    @Column(nullable = true)
    private String descrption;

    @Column(nullable = false)
    private Boolean contentWarning = false;

    @Column(nullable = false)
    private Boolean moderated = false;

    @CreationTimestamp
    @Column(nullable = false)
    private Instant creationDate;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedDate;

    @ElementCollection
    @OrderBy("tags")
    private List<String> tags = new ArrayList<>();

    @OneToMany
    private List<Heart> hearts = new ArrayList<>();

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        return Objects.equals(id, ((Publication)obj).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
