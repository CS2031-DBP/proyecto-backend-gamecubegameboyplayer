package com.artpond.backend.publication.domain.hearts;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class HeartID implements Serializable {
    private Long userId;
    private Long publId;

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        return Objects.equals(publId, ((HeartID)obj).publId) && Objects.equals(userId, ((HeartID)obj).userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publId, userId);
    }
}
