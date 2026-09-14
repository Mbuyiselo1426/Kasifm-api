package com.kasiefm.api.model;

import jakarta.validation.constraints.NotNull;

public class UpdateMessageStatusRequest {
    @NotNull private MessageStatus status;
    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }
}
