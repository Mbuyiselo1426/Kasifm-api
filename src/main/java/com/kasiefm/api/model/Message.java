package com.kasiefm.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String senderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MessageCategory category;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 255)
    private String songTitle;

    @Column(length = 255)
    private String artist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MessageStatus status;

    // Nullable while existing production messages are migrated without a session.
    @ManyToOne
    @JoinColumn(name = "show_session_id")
    private ShowSession showSession;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Message() { }

    public Message(String senderName, MessageCategory category, String message, String songTitle, String artist) {
        this.senderName = senderName;
        this.category = category;
        this.message = message;
        this.songTitle = songTitle;
        this.artist = artist;
    }

    @PrePersist void initializeServerFields() {
        status = MessageStatus.NEW;
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSenderName() { return senderName; }
    public MessageCategory getCategory() { return category; }
    public String getMessage() { return message; }
    public String getSongTitle() { return songTitle; }
    public String getArtist() { return artist; }
    public MessageStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public ShowSession getShowSession() { return showSession; }
    public void setStatus(MessageStatus status) { this.status = status; }
    public void setShowSession(ShowSession showSession) { this.showSession = showSession; }
}
