package com.kasiefm.api.service;

import com.kasiefm.api.model.Show;
import com.kasiefm.api.model.ShowDto;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Daily repeating schedule, evaluated once per response in station time. */
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
        LocalTime now = LocalTime.ofInstant(clock.instant(), STATION_ZONE);
        Show current = shows.stream()
                .filter(show -> contains(show, now))
                .min(Comparator.comparing(Show::getId))
                .orElse(null);
        return shows.stream()
                .sorted(Comparator.comparing(Show::getStartTime).thenComparing(Show::getId))
                .map(show -> ShowDto.fromEntity(show, show == current))
                .collect(Collectors.toList());
    }

    private boolean contains(Show show, LocalTime now) {
        LocalTime start = show.getStartTime();
        LocalTime end = show.getEndTime();
        if (start.equals(end)) {
            return false; // zero-duration slot
        }
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        }
        return !now.isBefore(start) || now.isBefore(end); // crosses midnight
    }
}
