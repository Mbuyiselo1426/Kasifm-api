package com.kasiefm.api.controller;

import com.kasiefm.api.model.Show;
import com.kasiefm.api.model.ShowDto;
import com.kasiefm.api.model.UpdateShowRequest;
import com.kasiefm.api.repository.ShowRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.time.format.DateTimeParseException;
import java.util.List;
import com.kasiefm.api.service.ScheduleMapper;

@RestController
@RequestMapping("/api")
public class ScheduleController {

    private final ShowRepository showRepository;

    private final ScheduleMapper scheduleMapper;

    public ScheduleController(ShowRepository showRepository, ScheduleMapper scheduleMapper) {
        this.showRepository = showRepository;
        this.scheduleMapper = scheduleMapper;
    }

    // GET /api/schedule - the full day's lineup, in order.
    // This is what replaces the hardcoded showTitle/showTime strings in MainActivity.
    @GetMapping("/schedule")
    public List<ShowDto> getSchedule() {
        ZonedDateTime now = scheduleMapper.now();
        List<Show> shows = new ArrayList<>(showRepository
                .findByDayOfWeekOrderByStartTimeAscIdAsc(now.getDayOfWeek()));
        // Keep an active previous-day overnight row visible to marker-based clients.
        showRepository.findByDayOfWeekOrderByStartTimeAscIdAsc(now.minusDays(1).getDayOfWeek())
                .stream().filter(show -> scheduleMapper.contains(show, now)).forEach(shows::add);
        return scheduleMapper.map(shows, now);
    }

    // GET /api/schedule/{id} - a single show, useful later for a "show details" screen.
    @GetMapping("/schedule/{id}")
    public ShowDto getShow(@PathVariable Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id));
        return currentDto(show);
    }

    // PUT /api/schedule/{id} - update an existing show in the schedule.
    @PutMapping("/schedule/{id}")
    public ShowDto updateShow(@PathVariable Long id, @Valid @RequestBody UpdateShowRequest request) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id));

        LocalTime startTime = parseTime(request.getStartTime(), "startTime");
        LocalTime endTime = parseTime(request.getEndTime(), "endTime");
        if (endTime.equals(startTime)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "endTime must differ from startTime"
            );
        }

        show.setDayOfWeek(request.getDayOfWeek());
        show.setName(request.getName().trim());
        show.setPresenter(trimToNull(request.getPresenter()));
        show.setStartTime(startTime);
        show.setEndTime(endTime);
        show.setDescription(trimToNull(request.getDescription()));

        return currentDto(showRepository.save(show));
    }

    private ShowDto currentDto(Show show) {
        return getSchedule().stream()
                .filter(dto -> dto.getId().equals(show.getId()))
                .findFirst()
                .orElseGet(() -> ShowDto.fromEntity(show));
    }

    private LocalTime parseTime(String value, String fieldName) {
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must use HH:mm format"
            );
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
