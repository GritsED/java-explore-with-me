package ru.practicum.stats;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointHitRequest {
    String app;
    String uri;
    String ip;
    LocalDateTime timestamp;
}
