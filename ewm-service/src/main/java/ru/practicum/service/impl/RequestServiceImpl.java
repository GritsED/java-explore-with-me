package ru.practicum.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;
import ru.practicum.model.enums.RequestStatus;
import ru.practicum.model.enums.State;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.interfaces.RequestService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getRequestsPrivate(Long userId) {
        User user = getUserOrThrow(userId);

        List<ParticipationRequest> allByRequesterId = requestRepository.findAllByRequesterId(userId);

        return allByRequesterId.stream().map(requestMapper::toDto).toList();
    }

    @Override
    public ParticipationRequestDto addRequestPrivate(Long userId, Long eventId) {
        User requester = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists.");
        }

        if (event.getInitiator().equals(requester)) {
            throw new ConflictException("You can't add a request to your own event.");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("You can add a request only to a published event.");
        }

        if (event.getParticipantLimit() > 0 &&
                event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            throw new ConflictException("The limit for participation requests has been reached.");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(event.getRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build();

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelUserRequestPrivate(Long userId, Long requestId) {
        User requester = getUserOrThrow(userId);
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found."));


        if (!request.getRequester().equals(requester)) {
            throw new ConflictException("You can cancel only your own participation request.");
        }

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            Event event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        request.setStatus(RequestStatus.CANCELED);

        return requestMapper.toDto(requestRepository.save(request));
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }
}
