package com.kasiefm.api.model;

import java.time.Instant;

public record MessageDto(Long id, String senderName, MessageCategory category, String message,
                         String songTitle, String artist, MessageStatus status, Instant createdAt,
                         Long showSessionId, String showName) {
    public static MessageDto fromEntity(Message message) {
        ShowSession session = message.getShowSession();
        return new MessageDto(message.getId(), message.getSenderName(), message.getCategory(),
                message.getMessage(), message.getSongTitle(), message.getArtist(),
                message.getStatus(), message.getCreatedAt(),
                session == null ? null : session.getId(),
                session == null ? null : session.getShow().getName());
    }
}
