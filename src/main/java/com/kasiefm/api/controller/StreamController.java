package com.kasiefm.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StreamController {

    // Same placeholder SomaFM URL your Android app uses today, but now it lives in
    // config (application.properties / env var) instead of being hardcoded in Java.
    // Swap KASIE_STREAM_URL when you get a real stream URL from the station - no app
    // rebuild required, since the Android app will fetch this from the API (Phase 2).
    @Value("${kasie.stream-url:https://ice1.somafm.com/groovesalad-128-mp3}")
    private String streamUrl;

    @Value("${kasie.stream-url-lite:https://ice1.somafm.com/groovesalad-64-aac}")
    private String streamUrlLite;

    // GET /api/stream-url - what replaces the RADIO_STREAM_URL constant in
    // MainActivity.java and RadioService.java.
    @GetMapping("/stream-url")
    public Map<String, String> getStreamUrl() {
        return Map.of(
                "streamUrl", streamUrl,
                "streamUrlLite", streamUrlLite // for the Data Lite mode in Phase 4
        );
    }
}
