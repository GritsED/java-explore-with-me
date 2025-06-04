package ru.practicum.service.interfaces;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.request.*;
import ru.practicum.dto.response.EventFullDto;
import ru.practicum.dto.response.EventRequestStatusUpdateResult;
import ru.practicum.dto.response.EventShortDto;

import java.util.List;

public interface EventService {
    List<EventShortDto> getUserEventsPrivate(Long userId, Integer from, Integer size);

    EventFullDto addNewEventPrivate(NewEventDto dto, Long userId);

    EventFullDto getEventByUserPrivate(Long userId, Long eventId);

    EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest dto);

    List<ParticipationRequestDto> getUserEventRequestsPrivate(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestsPrivate(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest dto);

    List<EventFullDto> getEventsAdmin(EventSearchParamsAdmin params, Integer from, Integer size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest dto);

    List<EventShortDto> getEventsPublic(EventSearchParamsPublic params, Integer from, Integer size,
                                        HttpServletRequest httpServletRequest);

    EventFullDto getEventByIdPublic(Long eventId, HttpServletRequest httpServletRequest);
}
