package ru.practicum.stats.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.stats.EndpointHitRequest;
import ru.practicum.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper
public interface EndpointHitMapper {
    @Mapping(source = "timestamp", target = "timestamp")
    EndpointHit toEntity(EndpointHitRequest dto);

    default LocalDateTime map(String value) {
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
