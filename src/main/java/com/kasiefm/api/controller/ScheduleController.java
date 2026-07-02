package com.kasiefm.api.controller;

import com.kasiefm.api.model.Show;
import com.kasiefm.api.model.ShowDto;
import com.kasiefm.api.repository.ShowRepository;
import org.springframework.web.bind.annotation.*;

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
}
