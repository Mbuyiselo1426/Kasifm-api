package com.kasiefm.api.controller;

import com.kasiefm.api.model.MessageDto;
import com.kasiefm.api.model.ShowSessionHistoryDto;
import com.kasiefm.api.repository.MessageRepository;
import com.kasiefm.api.repository.ShowSessionRepository;
import com.kasiefm.api.service.ShowSessionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/show-sessions")
public class ShowSessionController {
    private static final int RECENT_SESSION_LIMIT = 20;

    private final ShowSessionRepository showSessionRepository;
    private final MessageRepository messageRepository;
    private final ShowSessionService showSessionService;

    public ShowSessionController(ShowSessionRepository showSessionRepository, MessageRepository messageRepository,
                                 ShowSessionService showSessionService) {
        this.showSessionRepository = showSessionRepository;
        this.messageRepository = messageRepository;
        this.showSessionService = showSessionService;
    }

    @GetMapping("/recent")
    public List<ShowSessionHistoryDto> recent() {
        Long currentSessionId = showSessionService.findCurrentSession().map(session -> session.getId()).orElse(null);
        return showSessionRepository.findRecentHistory(currentSessionId, PageRequest.of(0, RECENT_SESSION_LIMIT));
    }

    @GetMapping("/{sessionId}/messages")
    public List<MessageDto> messages(@PathVariable Long sessionId) {
        if (!showSessionRepository.existsById(sessionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Show session not found");
        }
        return messageRepository.findByShowSessionIdOrderByCreatedAtDescIdDesc(sessionId).stream()
                .map(MessageDto::fromEntity).toList();
    }
}
