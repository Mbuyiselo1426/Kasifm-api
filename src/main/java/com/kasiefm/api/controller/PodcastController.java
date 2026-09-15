package com.kasiefm.api.controller;

import com.kasiefm.api.model.PodcastEpisodeDto;
import com.kasiefm.api.repository.PodcastEpisodeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/podcasts")
public class PodcastController {
    private final PodcastEpisodeRepository podcastEpisodeRepository;

    public PodcastController(PodcastEpisodeRepository podcastEpisodeRepository) {
        this.podcastEpisodeRepository = podcastEpisodeRepository;
    }

    @GetMapping
    public List<PodcastEpisodeDto> listPublished() {
        return podcastEpisodeRepository.findByPublishedTrueOrderByCreatedAtDescIdDesc().stream()
                .map(PodcastEpisodeDto::fromEntity)
                .toList();
    }
}
