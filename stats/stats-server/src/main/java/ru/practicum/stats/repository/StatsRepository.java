package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.EndpointStatsResponse;
import ru.practicum.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("select  new ru.practicum.stats.EndpointStatsResponse(eh.app, eh.uri , COUNT(eh)) " +
            "from EndpointHit eh \n" +
            "where (eh.timestamp between :start and :end)\n" +
            "group by eh.app, eh.uri")
    List<EndpointStatsResponse> findStats(LocalDateTime start, LocalDateTime end);

    @Query("select  new ru.practicum.stats.EndpointStatsResponse(eh.app, eh.uri , COUNT(DISTINCT eh.ip)) " +
            "from EndpointHit eh \n" +
            "where eh.timestamp between :start and :end\n" +
            "group by eh.app, eh.uri")
    List<EndpointStatsResponse> findUniqueStats(LocalDateTime start, LocalDateTime end);

    @Query("select  new ru.practicum.stats.EndpointStatsResponse(eh.app, eh.uri , COUNT(eh)) " +
            "from EndpointHit eh \n" +
            "where eh.timestamp between :start and :end\n" +
            "and eh.uri in :uris\n" +
            "group by eh.app, eh.uri" +
            "order by COUNT(eh) DESC")
    List<EndpointStatsResponse> findAllStatsWithUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select  new ru.practicum.stats.EndpointStatsResponse(eh.app, eh.uri , COUNT(DISTINCT eh.ip)) " +
            "from EndpointHit eh \n" +
            "where eh.timestamp between :start and :end\n" +
            "and eh.uri in :uris\n" +
            "group by eh.app, eh.uri" +
            "group by COUNT(DISTINCT eh.ip) DESC")
    List<EndpointStatsResponse> findAllUniqueStatsWithUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}
