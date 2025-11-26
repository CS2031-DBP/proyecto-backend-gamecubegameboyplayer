package com.artpond.backend.publication.infrastructure;

public interface HeartCountProjection {
    Long getPublicationId();
    Long getCount();
}