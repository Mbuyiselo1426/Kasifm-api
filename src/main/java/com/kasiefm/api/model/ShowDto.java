package com.kasiefm.api.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * What the Android app actually receives from GET /api/schedule.
 * Keeps the wire format stable even if the entity changes later.
 */
public class ShowDto {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private Long id;
    private String name;
    private String presenter;
    private String startTime;
    private String endTime;
    private String description;

    public static ShowDto fromEntity(Show show) {
        ShowDto dto = new ShowDto();
        dto.id = show.getId();
        dto.name = show.getName();
        dto.presenter = show.getPresenter();
        dto.startTime = show.getStartTime().format(FORMAT);
        dto.endTime = show.getEndTime().format(FORMAT);
        dto.description = show.getDescription();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPresenter() {
        return presenter;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDescription() {
        return description;
    }
}
