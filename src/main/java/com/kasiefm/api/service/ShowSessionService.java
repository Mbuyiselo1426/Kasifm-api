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
        return Optional.of(showSessionRepository.findByShowIdAndSessionDate(current.getId(), sessionDate)
                .orElseGet(() -> showSessionRepository.save(new ShowSession(
                        current,
                        sessionDate,
                        sessionDate.atTime(current.getStartTime()).atZone(now.getZone()).toInstant(),
                        sessionDate.plusDays(current.getEndTime().isAfter(current.getStartTime()) ? 0 : 1)
                                .atTime(current.getEndTime()).atZone(now.getZone()).toInstant()))));
    }
}
