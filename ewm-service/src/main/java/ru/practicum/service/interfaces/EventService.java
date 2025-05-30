package ru.practicum.service.interfaces;

import ru.practicum.dto.request.NewEventDto;
import ru.practicum.dto.request.UpdateEventAdminRequest;
import ru.practicum.dto.request.UpdateEventUserRequest;
import ru.practicum.dto.response.EventFullDto;
import ru.practicum.dto.response.EventShortDto;

import java.util.List;

public interface EventService {
    List<EventShortDto> getUserEventsPrivate(Long userId, Integer from, Integer size);

    EventFullDto addNewEventPrivate(NewEventDto dto, Long userId);

    EventFullDto getEventByUserPrivate(Long userId, Long eventId);

    EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest dto);

    List<EventFullDto> getEventsAdmin(List<Long> users,
                                      List<String> states,
                                      List<Long> categories,
                                      String rangeStart,
                                      String rangeEnd,
                                      Integer from,
                                      Integer size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest dto);
}
