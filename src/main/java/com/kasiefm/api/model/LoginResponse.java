package com.kasiefm.api.model;

public record LoginResponse(String token, String displayName, UserRole role) { }
