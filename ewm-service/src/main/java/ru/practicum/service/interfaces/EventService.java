package ru.practicum.service.interfaces;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.request.NewEventDto;
import ru.practicum.dto.request.UpdateEventAdminRequest;
import ru.practicum.dto.request.UpdateEventUserRequest;
import ru.practicum.dto.response.EventFullDto;
import ru.practicum.dto.response.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventShortDto> getUserEventsPrivate(Long userId, Integer from, Integer size);

    EventFullDto addNewEventPrivate(NewEventDto dto, Long userId);

    EventFullDto getEventByUserPrivate(Long userId, Long eventId);

    EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest dto);

    List<EventFullDto> getEventsAdmin(List<Long> users, List<String> states, List<Long> categories,
                                      String rangeStart, String rangeEnd, Integer from, Integer size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest dto);

    List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                        String sortOpt, Integer from, Integer size,
                                        HttpServletRequest httpServletRequest);

    EventFullDto getEventByIdPublic(Long eventId, HttpServletRequest httpServletRequest);
}
