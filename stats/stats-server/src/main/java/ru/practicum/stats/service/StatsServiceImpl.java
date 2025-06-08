package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.stats.EndpointHitRequest;
import ru.practicum.stats.EndpointStatsResponse;
import ru.practicum.stats.exception.ValidationException;
import ru.practicum.stats.mapper.EndpointHitMapper;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final EndpointHitMapper endpointHitMapper;

    @Override
    public void saveHit(EndpointHitRequest dto) {
        EndpointHit save = statsRepository.save(endpointHitMapper.toEntity(dto));
    }

    @Override
    public List<EndpointStatsResponse> findStats(LocalDateTime start, LocalDateTime end,
                                                 List<String> uris, Boolean unique) {

        end = end.plusSeconds(1);

        if (start.isAfter(end)) {
            throw new ValidationException("Start date cannot be after end date");
        }

        List<EndpointStatsResponse> result;

        if (uris == null || uris.isEmpty()) {
            if (!unique) {
                result = statsRepository.findStats(start, end);
            } else {
                result = statsRepository.findUniqueStats(start, end);
            }
        } else {
            if (!unique) {
                result = statsRepository.findAllStatsWithUris(start, end, uris);
            } else {
                result = statsRepository.findAllUniqueStatsWithUris(start, end, uris);
            }
        }
        return result;
    }
}
