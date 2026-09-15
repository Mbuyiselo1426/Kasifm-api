package com.kasiefm.api.controller;

import com.kasiefm.api.model.PodcastEpisode;
import com.kasiefm.api.repository.PodcastEpisodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PodcastControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private PodcastEpisodeRepository podcastEpisodeRepository;

    @BeforeEach
    void setUp() {
        podcastEpisodeRepository.deleteAll();
    }

    @Test
    void listsOnlyPublishedEpisodesWithoutAuthentication() throws Exception {
        PodcastEpisode published = podcastEpisodeRepository.save(new PodcastEpisode(
                "Published episode", "Morning Essentials", null, "https://example.test/published.mp3",
                null, true));
        podcastEpisodeRepository.save(new PodcastEpisode(
                "Draft episode", "Morning Essentials", "Not public", "https://example.test/draft.mp3",
                Instant.parse("2026-09-15T08:00:00Z"), false));

        mockMvc.perform(get("/api/podcasts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(published.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is("Published episode")))
                .andExpect(jsonPath("$[0].showName", is("Morning Essentials")))
                .andExpect(jsonPath("$[0].audioUrl", is("https://example.test/published.mp3")))
                .andExpect(jsonPath("$[0].description").doesNotExist())
                .andExpect(jsonPath("$[0].publishedAt").doesNotExist())
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].published", is(true)));
    }
}
