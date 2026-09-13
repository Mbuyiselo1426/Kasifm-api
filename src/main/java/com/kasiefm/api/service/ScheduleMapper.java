package com.kasiefm.api.service;

import com.kasiefm.api.model.Show;
import com.kasiefm.api.model.ShowDto;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Weekly schedule; a show belongs to the Johannesburg day on which it starts. */
@Component
public class ScheduleMapper {
    private static final ZoneId STATION_ZONE = ZoneId.of("Africa/Johannesburg");
    private final Clock clock;

    public ScheduleMapper() {
        this(Clock.systemUTC());
    }

    public ScheduleMapper(Clock clock) {
        this.clock = clock;
    }

    public List<ShowDto> map(List<Show> shows) {
        return map(shows, now());
    }

    public ZonedDateTime now() {
        return clock.instant().atZone(STATION_ZONE);
    }

    public List<ShowDto> map(List<Show> shows, ZonedDateTime instant) {
        ZonedDateTime now = instant.withZoneSameInstant(STATION_ZONE);
        Show current = shows.stream()
                .filter(show -> contains(show, now))
                .min(Comparator.comparing(Show::getId))
                .orElse(null);
        return shows.stream()
                .sorted(Comparator.comparing(Show::getStartTime).thenComparing(Show::getId))
                .map(show -> ShowDto.fromEntity(show, show == current))
                .collect(Collectors.toList());
    }

    public boolean contains(Show show, ZonedDateTime instant) {
        ZonedDateTime stationNow = instant.withZoneSameInstant(STATION_ZONE);
        LocalTime now = stationNow.toLocalTime();
        LocalTime start = show.getStartTime();
        LocalTime end = show.getEndTime();
        if (show.getDayOfWeek() == null || start.equals(end)) {
            return false;
        }
        boolean startsToday = show.getDayOfWeek() == stationNow.getDayOfWeek();
        if (start.isBefore(end)) {
            return startsToday && !now.isBefore(start) && now.isBefore(end);
        }
        boolean startedYesterday = show.getDayOfWeek() == stationNow.minusDays(1).getDayOfWeek();
        return (startsToday && !now.isBefore(start)) || (startedYesterday && now.isBefore(end));
    }
}
