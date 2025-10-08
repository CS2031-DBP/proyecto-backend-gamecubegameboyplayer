package com.artpond.backend.publication.domain.hearts;

import java.util.Objects;

import com.artpond.backend.publication.domain.Publication;
import com.artpond.backend.user.domain.User;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.Data;

@Data
@Entity
public class Heart {
    @EmbeddedId
    private HeartID id;

    @MapsId("publId")
    @ManyToOne
    @JoinColumn(nullable = false)
    private Publication post;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(nullable = false)
    private User author;

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        return Objects.equals(id, ((Heart)obj).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
