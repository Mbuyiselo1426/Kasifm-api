package com.kasiefm.api.config;

import com.kasiefm.api.model.PodcastEpisode;
import com.kasiefm.api.repository.PodcastEpisodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "app.seed.enabled=true", showSql = false)
@ActiveProfiles("test")
@Import(PodcastEpisodeSeeder.class)
class PodcastEpisodeSeederTest {
    @Autowired private PodcastEpisodeSeeder seeder;
    @Autowired private PodcastEpisodeRepository podcastEpisodeRepository;

    @BeforeEach
    void setUp() {
        podcastEpisodeRepository.deleteAllInBatch();
    }

    @Test
    void seedsOnlyTheVerifiedEpisodeWithoutInventedMetadata() {
        seeder.run();

        assertEquals(1, podcastEpisodeRepository.count());
        PodcastEpisode episode = podcastEpisodeRepository.findAll().get(0);
        assertEquals("CLINIX Interview", episode.getTitle());
        assertEquals("Morning Essentials", episode.getShowName());
        assertEquals(PodcastEpisodeSeeder.CLINIX_INTERVIEW_AUDIO_URL, episode.getAudioUrl());
        assertNull(episode.getDescription());
        assertNull(episode.getPublishedAt());
        assertTrue(episode.isPublished());
        assertFalse(episode.getCreatedAt() == null);
    }

    @Test
    void seedingIsIdempotentByAudioUrl() {
        seeder.run();
        Long id = podcastEpisodeRepository.findAll().get(0).getId();
        seeder.run();

        assertEquals(1, podcastEpisodeRepository.count());
        assertEquals(id, podcastEpisodeRepository.findAll().get(0).getId());
    }
}
