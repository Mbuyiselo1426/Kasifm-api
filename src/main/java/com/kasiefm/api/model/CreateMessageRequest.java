package com.kasiefm.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateMessageRequest {
    @NotBlank @Size(max = 255) private String senderName;
    @NotNull private MessageCategory category;
    @NotBlank @Size(max = 500) private String message;
    @Size(max = 255) private String songTitle;
    @Size(max = 255) private String artist;

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public MessageCategory getCategory() { return category; }
    public void setCategory(MessageCategory category) { this.category = category; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSongTitle() { return songTitle; }
    public void setSongTitle(String songTitle) { this.songTitle = songTitle; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
}
