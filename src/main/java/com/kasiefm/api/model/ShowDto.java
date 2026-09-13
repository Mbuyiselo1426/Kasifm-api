package com.kasiefm.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private boolean current;

    public static ShowDto fromEntity(Show show) {
        return fromEntity(show, false);
    }

    public static ShowDto fromEntity(Show show, boolean current) {
        ShowDto dto = new ShowDto();
        dto.current = current;
        dto.id = show.getId();
        dto.name = show.getName();
        dto.presenter = show.getPresenter();
        dto.startTime = show.getStartTime().format(FORMAT);
        dto.endTime = show.getEndTime().format(FORMAT);
        dto.description = show.getDescription();
        return dto;
    }

    @JsonProperty("isCurrent")
    public boolean isCurrent() {
        return current;
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
