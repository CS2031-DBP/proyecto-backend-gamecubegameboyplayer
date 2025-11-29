package com.artpond.backend.map.domain;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "places", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"osmId", "osmType"})
})
@Data
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = false)
    private Long osmId;

    @Column(nullable = false)
    private String osmType;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String address;

    @Column(columnDefinition = "geography(Point, 4326)", nullable = false, unique = true)
    private Point coordinates;

    @Column(nullable = false)
    private Long postCount = 0L;
}
