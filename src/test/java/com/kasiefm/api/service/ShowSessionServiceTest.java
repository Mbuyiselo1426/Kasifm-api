package com.kasiefm.api.service;

import com.kasiefm.api.model.Show;
import com.kasiefm.api.model.ShowSession;
import com.kasiefm.api.repository.ShowRepository;
import com.kasiefm.api.repository.ShowSessionRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ShowSessionServiceTest {
    @Test
    void repeatedResolutionUsesOneSessionForTheSameAiring() {
        ShowRepository shows = mock(ShowRepository.class);
        ShowSessionRepository sessions = mock(ShowSessionRepository.class);
        Show current = show(1L, DayOfWeek.SUNDAY, "21:00", "00:00");
        when(shows.findByDayOfWeekOrderByStartTimeAscIdAsc(DayOfWeek.SUNDAY)).thenReturn(List.of(current));
        when(shows.findByDayOfWeekOrderByStartTimeAscIdAsc(DayOfWeek.SATURDAY)).thenReturn(List.of());
        AtomicReference<ShowSession> stored = new AtomicReference<>();
        when(sessions.findByShowIdAndSessionDate(eq(1L), eq(LocalDate.of(2026, 9, 13))))
                .thenAnswer(invocation -> java.util.Optional.ofNullable(stored.get()));
        when(sessions.save(any(ShowSession.class))).thenAnswer(invocation -> {
            stored.set(invocation.getArgument(0));
            return stored.get();
        });

        ShowSessionService service = new ShowSessionService(shows, sessions,
                mapperAt("2026-09-13T21:45:00Z")); // 23:45 Sunday in Johannesburg
        ShowSession first = service.resolveCurrentSession().orElseThrow();
        ShowSession second = service.resolveCurrentSession().orElseThrow();

        assertSame(first, second);
        assertEquals(LocalDate.of(2026, 9, 13), first.getSessionDate());
        assertEquals(Instant.parse("2026-09-13T19:00:00Z"), first.getStartsAt());
        assertEquals(Instant.parse("2026-09-13T22:00:00Z"), first.getEndsAt());
        verify(sessions, times(1)).save(any(ShowSession.class));
    }

    @Test
    void midnightShowUsesTheNewJohannesburgCalendarDay() {
        ShowRepository shows = mock(ShowRepository.class);
        ShowSessionRepository sessions = mock(ShowSessionRepository.class);
        Show midnight = show(2L, DayOfWeek.MONDAY, "00:00", "03:00");
        when(shows.findByDayOfWeekOrderByStartTimeAscIdAsc(DayOfWeek.MONDAY)).thenReturn(List.of(midnight));
        when(shows.findByDayOfWeekOrderByStartTimeAscIdAsc(DayOfWeek.SUNDAY)).thenReturn(List.of());
        when(sessions.findByShowIdAndSessionDate(anyLong(), any())).thenReturn(java.util.Optional.empty());
        when(sessions.save(any(ShowSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShowSession session = new ShowSessionService(shows, sessions,
                mapperAt("2026-09-13T22:15:00Z")).resolveCurrentSession().orElseThrow();

        assertEquals(LocalDate.of(2026, 9, 14), session.getSessionDate());
        assertEquals(Instant.parse("2026-09-13T22:00:00Z"), session.getStartsAt());
        assertEquals(Instant.parse("2026-09-14T01:00:00Z"), session.getEndsAt());
    }

    @Test
    void currentSessionLookupDoesNotCreateAHistoryReadSession() {
        ShowRepository shows = mock(ShowRepository.class);
        ShowSessionRepository sessions = mock(ShowSessionRepository.class);
        Show current = show(3L, DayOfWeek.SUNDAY, "21:00", "00:00");
        when(shows.findByDayOfWeekOrderByStartTimeAscIdAsc(DayOfWeek.SUNDAY)).thenReturn(List.of(current));
        when(shows.findByDayOfWeekOrderByStartTimeAscIdAsc(DayOfWeek.SATURDAY)).thenReturn(List.of());
        when(sessions.findByShowIdAndSessionDate(anyLong(), any())).thenReturn(java.util.Optional.empty());

        assertTrue(new ShowSessionService(shows, sessions, mapperAt("2026-09-13T21:45:00Z"))
                .findCurrentSession().isEmpty());
        verify(sessions, never()).save(any());
    }

    private ScheduleMapper mapperAt(String instant) {
        return new ScheduleMapper(Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }

    private Show show(Long id, DayOfWeek day, String start, String end) {
        Show show = new Show("Test show", null, LocalTime.parse(start), LocalTime.parse(end), null, day);
        show.setId(id);
        return show;
    }
}
