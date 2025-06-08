package ru.practicum.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getRequestsPrivate(Long userId) {
        log.info("[getRequestsPrivate] Fetching requests for userId={}", userId);
        getUserOrThrow(userId);

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);
        log.info("[getRequestsPrivate] Found {} requests for userId={}", requests.size(), userId);

        return requests.stream().map(requestMapper::toDto).toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequestPrivate(Long userId, Long eventId) {
        log.info("[addRequestPrivate] User {} is trying to add request to event {}", userId, eventId);

        User requester = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            log.warn("[addRequestPrivate] Conflict: Request already exists for user {} on event {}", userId, eventId);
            throw new ConflictException("Request already exists.");
        }

        if (event.getInitiator().equals(requester)) {
            log.warn("[addRequestPrivate] Conflict: User {} tried to add request to own event {}", userId, eventId);
            throw new ConflictException("You can't add a request to your own event.");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            log.warn("[addRequestPrivate] Conflict: Event {} is not published", eventId);
            throw new ConflictException("You can add a request only to a published event.");
        }

        if (event.getParticipantLimit() > 0 &&
                event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            log.warn("[addRequestPrivate] Conflict: Participant limit reached for event {}", eventId);
            throw new ConflictException("The limit for participation requests has been reached.");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(event.getParticipantLimit() != 0 && event.getRequestModeration() ?
                                RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build();

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
            log.info("[addRequestPrivate] Incremented confirmed requests for event {}", eventId);
        }

        ParticipationRequestDto dto = requestMapper.toDto(requestRepository.save(request));
        log.info("[addRequestPrivate] Saved participation request with id {}", dto.getId());
        return dto;
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelUserRequestPrivate(Long userId, Long requestId) {
        log.info("[cancelUserRequestPrivate] User {} requests cancellation of participation request {}",
                 userId, requestId);
        User requester = getUserOrThrow(userId);
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(ParticipationRequest.class, requestId));


        if (!request.getRequester().equals(requester)) {
            log.warn("[cancelUserRequestPrivate] User {} tried to cancel someone else's request {}", userId, requestId);
            throw new ConflictException("You can cancel only your own participation request.");
        }

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            Event event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
            log.info("[cancelUserRequestPrivate] Decremented confirmed requests count for event {}", event.getId());
        }

        request.setStatus(RequestStatus.CANCELED);
        log.info("[cancelUserRequestPrivate] Participation request {} status changed to CANCELED", requestId);

        return requestMapper.toDto(requestRepository.save(request));
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(Event.class, eventId));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(User.class, userId));
    }
}
