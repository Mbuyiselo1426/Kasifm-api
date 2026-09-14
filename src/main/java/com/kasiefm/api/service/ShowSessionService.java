package com.kasiefm.api.service;

import com.kasiefm.api.model.Show;
import com.kasiefm.api.model.ShowSession;
import com.kasiefm.api.repository.ShowRepository;
import com.kasiefm.api.repository.ShowSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Resolves a recurring schedule row into the single airing happening now. */
@Service
public class ShowSessionService {
    private final ShowRepository showRepository;
    private final ShowSessionRepository showSessionRepository;
    private final ScheduleMapper scheduleMapper;

    public ShowSessionService(ShowRepository showRepository, ShowSessionRepository showSessionRepository,
                              ScheduleMapper scheduleMapper) {
        this.showRepository = showRepository;
        this.showSessionRepository = showSessionRepository;
        this.scheduleMapper = scheduleMapper;
    }

    @Transactional
    public Optional<ShowSession> resolveCurrentSession() {
        return currentAiring().map(airing -> showSessionRepository
                .findByShowIdAndSessionDate(airing.show().getId(), airing.sessionDate())
                .orElseGet(() -> showSessionRepository.save(new ShowSession(
                        airing.show(), airing.sessionDate(), airing.startsAt(), airing.endsAt()))));
    }

    /** Finds an already-created current session without creating one for a history read. */
    @Transactional(readOnly = true)
    public Optional<ShowSession> findCurrentSession() {
        return currentAiring().flatMap(airing -> showSessionRepository
                .findByShowIdAndSessionDate(airing.show().getId(), airing.sessionDate()));
    }

    private Optional<CurrentAiring> currentAiring() {
        ZonedDateTime now = scheduleMapper.now();
        List<Show> candidates = new ArrayList<>(showRepository
                .findByDayOfWeekOrderByStartTimeAscIdAsc(now.getDayOfWeek()));
        candidates.addAll(showRepository.findByDayOfWeekOrderByStartTimeAscIdAsc(
                now.minusDays(1).getDayOfWeek()));

        Show current = candidates.stream()
                .filter(show -> scheduleMapper.contains(show, now))
                .min(Comparator.comparing(Show::getId))
                .orElse(null);
        if (current == null) {
            return Optional.empty();
        }

        LocalDate sessionDate = current.getDayOfWeek() == now.getDayOfWeek()
                ? now.toLocalDate() : now.toLocalDate().minusDays(1);
        return Optional.of(new CurrentAiring(current, sessionDate,
                sessionDate.atTime(current.getStartTime()).atZone(now.getZone()).toInstant(),
                sessionDate.plusDays(current.getEndTime().isAfter(current.getStartTime()) ? 0 : 1)
                        .atTime(current.getEndTime()).atZone(now.getZone()).toInstant()));
    }

    private record CurrentAiring(Show show, LocalDate sessionDate,
                                 java.time.Instant startsAt, java.time.Instant endsAt) { }
}
