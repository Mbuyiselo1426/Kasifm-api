package com.kasiefm.api.model;

import java.time.Instant;
import java.time.LocalDate;

/** Lightweight metadata for the presenter history list. */
public record ShowSessionHistoryDto(Long sessionId, Long showId, String showName,
                                    LocalDate sessionDate, Instant startsAt, Instant endsAt,
                                    long messageCount) { }
