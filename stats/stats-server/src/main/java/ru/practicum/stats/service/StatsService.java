package ru.practicum.stats.service;

import ru.practicum.stats.EndpointHitRequest;
import ru.practicum.stats.EndpointStatsResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void saveHit(EndpointHitRequest dto);

    List<EndpointStatsResponse> findStats(LocalDateTime start, LocalDateTime end,
                                          List<String> uris, Boolean unique);
}
