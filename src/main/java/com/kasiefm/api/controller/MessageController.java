package com.kasiefm.api.controller;

import com.kasiefm.api.model.*;
import com.kasiefm.api.repository.MessageRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto create(@Valid @RequestBody CreateMessageRequest request) {
        if (request.getCategory() == MessageCategory.SONG_REQUEST && isBlank(request.getSongTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "songTitle is required for a song request");
        }
        Message message = new Message(trim(request.getSenderName()), request.getCategory(),
                trim(request.getMessage()), trimToNull(request.getSongTitle()), trimToNull(request.getArtist()));
        return MessageDto.fromEntity(messageRepository.save(message));
    }

    /** Presenter-facing endpoint: protect with authentication before production use. */
    @GetMapping
    public List<MessageDto> getMessages() {
        return messageRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(MessageDto::fromEntity).toList();
    }

    /** Presenter-facing endpoint: protect with authentication before production use. */
    @PatchMapping("/{id}/status")
    public MessageDto updateStatus(@PathVariable Long id,
                                   @Valid @RequestBody UpdateMessageStatusRequest request) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));
        message.setStatus(request.getStatus());
        return MessageDto.fromEntity(messageRepository.save(message));
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
    private String trim(String value) { return value.trim(); }
    private String trimToNull(String value) { return isBlank(value) ? null : value.trim(); }
}
