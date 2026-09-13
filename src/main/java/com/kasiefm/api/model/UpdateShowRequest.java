package com.kasiefm.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateShowRequest {

    @NotNull
    private DayOfWeek dayOfWeek;

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }

    // Parse names explicitly so JSON numeric enum ordinals are not accepted.
    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek == null ? null : DayOfWeek.valueOf(dayOfWeek);
    }


    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String presenter;

    @NotBlank
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "startTime must use HH:mm format")
    private String startTime;

    @NotBlank
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "endTime must use HH:mm format")
    private String endTime;

    @Size(max = 500)
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPresenter() {
        return presenter;
    }

    public void setPresenter(String presenter) {
        this.presenter = presenter;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
