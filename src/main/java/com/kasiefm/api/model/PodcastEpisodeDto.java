package com.kasiefm.api.model;

import java.time.Instant;

public record PodcastEpisodeDto(Long id, String title, String showName, String description,
                                String audioUrl, Instant publishedAt, Instant createdAt,
                                boolean published) {
    public static PodcastEpisodeDto fromEntity(PodcastEpisode episode) {
        return new PodcastEpisodeDto(episode.getId(), episode.getTitle(), episode.getShowName(),
                episode.getDescription(), episode.getAudioUrl(), episode.getPublishedAt(),
                episode.getCreatedAt(), episode.isPublished());
    }
}
