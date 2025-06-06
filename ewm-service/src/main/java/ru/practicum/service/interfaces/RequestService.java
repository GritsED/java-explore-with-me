package ru.practicum.service.interfaces;

import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getRequestsPrivate(Long id);

    ParticipationRequestDto addRequestPrivate(Long userId, Long eventId);

    ParticipationRequestDto cancelUserRequestPrivate(Long userId, Long eventId);
}
