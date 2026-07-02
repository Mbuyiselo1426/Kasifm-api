package com.kasiefm.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShowNotFoundException extends RuntimeException {
    public ShowNotFoundException(Long id) {
        super("No show found with id " + id);
    }
}
