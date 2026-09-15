package com.kasiefm.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "podcast_episodes")
public class PodcastEpisode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "show_name", nullable = false)
    private String showName;

    @Column(length = 2_000)
    private String description;

    @Column(name = "audio_url", nullable = false, unique = true, length = 2_000)
    private String audioUrl;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean published;

    public PodcastEpisode() {
    }

    public PodcastEpisode(String title, String showName, String description, String audioUrl,
                          Instant publishedAt, boolean published) {
        this.title = title;
        this.showName = showName;
        this.description = description;
        this.audioUrl = audioUrl;
        this.publishedAt = publishedAt;
        this.published = published;
    }

    @PrePersist
    void initializeCreatedAt() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getShowName() { return showName; }
    public String getDescription() { return description; }
    public String getAudioUrl() { return audioUrl; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isPublished() { return published; }
}
