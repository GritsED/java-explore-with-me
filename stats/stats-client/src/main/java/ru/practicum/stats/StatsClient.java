package ru.practicum.stats;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StatsClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    public void hit(EndpointHitRequest dto) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(serverUrl)
                .path("/hit")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHitRequest> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.POST, entity, Void.class);
    }

    public List<EndpointStatsResponse> findStats(LocalDateTime start, LocalDateTime end,
                                                 List<String> uris, Boolean unique) {
        String uri = UriComponentsBuilder.fromHttpUrl(serverUrl)
                .path("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .toUriString();

        ResponseEntity<List<EndpointStatsResponse>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<EndpointStatsResponse>>() {
                });

        return response.getBody();
    }
}
