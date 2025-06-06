package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findAllByInitiatorId(Long id, Pageable pageable);

    Optional<Event> findByInitiatorIdAndId(Long userId, Long eventId);

    boolean existsByCategoryId(Long categoryId);

    List<Event> findAllByIdIn(List<Long> eventIds);
}
