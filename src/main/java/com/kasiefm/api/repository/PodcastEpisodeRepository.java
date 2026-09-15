package com.kasiefm.api.repository;

import com.kasiefm.api.model.PodcastEpisode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PodcastEpisodeRepository extends JpaRepository<PodcastEpisode, Long> {
    List<PodcastEpisode> findByPublishedTrueOrderByCreatedAtDescIdDesc();

    Optional<PodcastEpisode> findByAudioUrl(String audioUrl);
}
