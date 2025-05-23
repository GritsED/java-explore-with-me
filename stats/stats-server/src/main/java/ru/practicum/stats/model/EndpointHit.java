package ru.practicum.stats.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "endpoint_hits")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "app", nullable = false)
    String app;
    @Column(name = "uri", nullable = false)
    String uri;
    @Column(name = "ip", nullable = false)
    String ip;
    @Column(name = "timestamp", nullable = false)
    LocalDateTime timestamp;
}
