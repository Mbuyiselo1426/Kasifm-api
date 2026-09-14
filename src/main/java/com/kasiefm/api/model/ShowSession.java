package com.kasiefm.api.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

/** One concrete airing of a recurring weekly show. */
@Entity
@Table(name = "show_sessions", uniqueConstraints = @UniqueConstraint(
        name = "uk_show_sessions_show_date", columnNames = {"show_id", "session_date"}))
public class ShowSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ShowSession() { }

    public ShowSession(Show show, LocalDate sessionDate, Instant startsAt, Instant endsAt) {
        this.show = show;
        this.sessionDate = sessionDate;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    @PrePersist void initializeCreatedAt() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public Show getShow() { return show; }
    public LocalDate getSessionDate() { return sessionDate; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public Instant getCreatedAt() { return createdAt; }
}
