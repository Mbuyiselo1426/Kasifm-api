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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // tighten this once you have a real domain/app in production
public class ScheduleController {

    private final ShowRepository showRepository;

    public ScheduleController(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    // GET /api/schedule - the full day's lineup, in order.
    // This is what replaces the hardcoded showTitle/showTime strings in MainActivity.
    @GetMapping("/schedule")
    public List<ShowDto> getSchedule() {
        return showRepository.findAllByOrderByStartTimeAsc()
                .stream()
                .map(ShowDto::fromEntity)
                .collect(Collectors.toList());
    }

    // GET /api/schedule/{id} - a single show, useful later for a "show details" screen.
    @GetMapping("/schedule/{id}")
    public ShowDto getShow(@PathVariable Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id));
        return ShowDto.fromEntity(show);
    }

    // PUT /api/schedule/{id} - update an existing show in the schedule.
    @PutMapping("/schedule/{id}")
    public ShowDto updateShow(@PathVariable Long id, @Valid @RequestBody UpdateShowRequest request) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id));

        LocalTime startTime = parseTime(request.getStartTime(), "startTime");
        LocalTime endTime = parseTime(request.getEndTime(), "endTime");
        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "endTime must be later than startTime"
            );
        }

        show.setName(request.getName().trim());
        show.setPresenter(trimToNull(request.getPresenter()));
        show.setStartTime(startTime);
        show.setEndTime(endTime);
        show.setDescription(trimToNull(request.getDescription()));

        return ShowDto.fromEntity(showRepository.save(show));
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
