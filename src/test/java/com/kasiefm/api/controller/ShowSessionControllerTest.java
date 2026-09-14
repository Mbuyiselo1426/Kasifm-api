package com.kasiefm.api.controller;

import com.kasiefm.api.model.*;
import com.kasiefm.api.repository.MessageRepository;
import com.kasiefm.api.repository.PresenterUserRepository;
import com.kasiefm.api.repository.ShowRepository;
import com.kasiefm.api.repository.ShowSessionRepository;
import com.kasiefm.api.service.JwtService;
import com.kasiefm.api.service.ScheduleMapper;
import com.kasiefm.api.service.ShowSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShowSessionControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ShowRepository showRepository;
    @Autowired private ShowSessionRepository showSessionRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private PresenterUserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private ScheduleMapper scheduleMapper;
    @Autowired private ShowSessionService showSessionService;

    @BeforeEach void setUp() {
        messageRepository.deleteAll();
        showSessionRepository.deleteAll();
        showRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test void recentSessionsAreNewestFirstWithMetadataCountsAndNoCurrentOrLegacyMessages() throws Exception {
        saveShowCoveringNow();
        ShowSession current = showSessionService.resolveCurrentSession().orElseThrow();

        Show olderShow = showRepository.save(show("Older Show", LocalTime.of(12, 0), LocalTime.of(15, 0)));
        Show newerShow = showRepository.save(show("Newer Show", LocalTime.of(18, 0), LocalTime.of(21, 0)));
        ShowSession older = session(olderShow, LocalDate.of(2026, 9, 13), "2026-09-13T10:00:00Z", "2026-09-13T13:00:00Z");
        ShowSession newer = session(newerShow, LocalDate.of(2026, 9, 14), "2026-09-14T16:00:00Z", "2026-09-14T19:00:00Z");
        addMessage(older, "Older session message");
        addMessage(newer, "Newest session message one");
        addMessage(newer, "Newest session message two");
        messageRepository.save(new Message("Legacy", MessageCategory.OTHER, "No session", null, null));

        mockMvc.perform(get("/api/show-sessions/recent").header("Authorization", "Bearer " + presenterToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].sessionId", is(newer.getId().intValue())))
                .andExpect(jsonPath("$[0].showId", is(newerShow.getId().intValue())))
                .andExpect(jsonPath("$[0].showName", is("Newer Show")))
                .andExpect(jsonPath("$[0].sessionDate", is("2026-09-14")))
                .andExpect(jsonPath("$[0].messageCount", is(2)))
                .andExpect(jsonPath("$[1].sessionId", is(older.getId().intValue())))
                .andExpect(jsonPath("$[1].messageCount", is(1)));
        assertEquals(4, messageRepository.count());
        assertEquals(3, showSessionRepository.count());

        mockMvc.perform(get("/api/show-sessions/recent")).andExpect(status().isUnauthorized());
        // The active session was deliberately created above and is excluded from this list.
        assertEquals(current.getId(), showSessionService.findCurrentSession().orElseThrow().getId());
    }

    @Test void historicalSessionMessagesAreScopedNewestFirstAndMissingSessionIsNotFound() throws Exception {
        Show targetShow = showRepository.save(show("History Target", LocalTime.of(9, 0), LocalTime.of(12, 0)));
        Show otherShow = showRepository.save(show("Other History", LocalTime.of(12, 0), LocalTime.of(15, 0)));
        ShowSession target = session(targetShow, LocalDate.of(2026, 9, 10), "2026-09-10T07:00:00Z", "2026-09-10T10:00:00Z");
        ShowSession other = session(otherShow, LocalDate.of(2026, 9, 10), "2026-09-10T10:00:00Z", "2026-09-10T13:00:00Z");
        Message older = addMessage(target, "Older target message");
        Thread.sleep(2L);
        Message newer = addMessage(target, "Newer target message");
        addMessage(other, "Other session message");
        messageRepository.save(new Message("Legacy", MessageCategory.OTHER, "No session", null, null));
        String token = presenterToken();

        mockMvc.perform(get("/api/show-sessions/{sessionId}/messages", target.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(newer.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(older.getId().intValue())))
                .andExpect(jsonPath("$[0].showSessionId", is(target.getId().intValue())));
        mockMvc.perform(get("/api/show-sessions/{sessionId}/messages", target.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/show-sessions/{sessionId}/messages", 999_999L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test void recentSessionsAreLimitedToTwentyPersistedSessions() throws Exception {
        Show show = showRepository.save(show("History Show", LocalTime.of(6, 0), LocalTime.of(9, 0)));
        List<ShowSession> sessions = new ArrayList<>();
        Instant firstStart = Instant.parse("2026-08-01T04:00:00Z");
        LocalDate firstDate = LocalDate.of(2026, 8, 1);
        for (int index = 0; index < 21; index++) {
            Instant startsAt = firstStart.plusSeconds(index * 86_400L);
            sessions.add(showSessionRepository.save(new ShowSession(show, firstDate.plusDays(index),
                    startsAt, startsAt.plusSeconds(10_800))));
        }

        mockMvc.perform(get("/api/show-sessions/recent").header("Authorization", "Bearer " + presenterToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(20)))
                .andExpect(jsonPath("$[0].sessionId", is(sessions.get(20).getId().intValue())))
                .andExpect(jsonPath("$[19].sessionId", is(sessions.get(1).getId().intValue())));
    }

    private Show show(String name, LocalTime start, LocalTime end) {
        return new Show(name, null, start, end, null, scheduleMapper.now().getDayOfWeek());
    }

    private ShowSession session(Show show, LocalDate date, String startsAt, String endsAt) {
        return showSessionRepository.save(new ShowSession(show, date, Instant.parse(startsAt), Instant.parse(endsAt)));
    }

    private Message addMessage(ShowSession session, String text) {
        Message message = new Message("Listener", MessageCategory.OTHER, text, null, null);
        message.setShowSession(session);
        return messageRepository.save(message);
    }

    private void saveShowCoveringNow() {
        ZonedDateTime now = scheduleMapper.now();
        LocalTime currentTime = now.toLocalTime().withNano(0);
        LocalTime start = currentTime.minusMinutes(1);
        LocalTime end = currentTime.plusMinutes(1);
        showRepository.save(new Show("Current Test Show", null, start, end, null,
                start.isAfter(currentTime) ? now.minusDays(1).getDayOfWeek() : now.getDayOfWeek()));
    }

    private String presenterToken() {
        PresenterUser user = userRepository.save(new PresenterUser("presenter-" + UUID.randomUUID(), passwordEncoder.encode("correct-password"),
                "Presenter", UserRole.PRESENTER));
        return jwtService.createToken(user);
    }
}
