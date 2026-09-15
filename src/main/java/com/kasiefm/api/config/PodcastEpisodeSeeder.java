package com.kasiefm.api.config;

import com.kasiefm.api.model.PodcastEpisode;
import com.kasiefm.api.repository.PodcastEpisodeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = false)
public class PodcastEpisodeSeeder implements CommandLineRunner {
    static final String CLINIX_INTERVIEW_AUDIO_URL =
            "https://kasiefm971.co.za/wp-content/uploads/2024/08/CLINIX-INTERVIEW.mp3";

    private final PodcastEpisodeRepository podcastEpisodeRepository;

    public PodcastEpisodeSeeder(PodcastEpisodeRepository podcastEpisodeRepository) {
        this.podcastEpisodeRepository = podcastEpisodeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (podcastEpisodeRepository.findByAudioUrl(CLINIX_INTERVIEW_AUDIO_URL).isPresent()) {
            return;
        }
        podcastEpisodeRepository.save(new PodcastEpisode("CLINIX Interview", "Morning Essentials", null,
                CLINIX_INTERVIEW_AUDIO_URL, null, true));
    }
}
