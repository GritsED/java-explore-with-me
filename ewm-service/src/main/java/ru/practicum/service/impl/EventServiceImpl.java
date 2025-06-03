package ru.practicum.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.*;
import ru.practicum.dto.response.EventFullDto;
import ru.practicum.dto.response.EventRequestStatusUpdateResult;
import ru.practicum.dto.response.EventShortDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.model.enums.RequestStatus;
import ru.practicum.model.enums.SortOpt;
import ru.practicum.model.enums.State;
import ru.practicum.model.enums.StateAction;
import ru.practicum.repository.*;
import ru.practicum.repository.specification.EventSpecification;
import ru.practicum.service.interfaces.EventService;
import ru.practicum.stats.EndpointHitRequest;
import ru.practicum.stats.EndpointStatsResponse;
import ru.practicum.stats.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<EventShortDto> getUserEventsPrivate(Long userId, Integer from, Integer size) {
        log.info("[getUserEventsPrivate] Request from userId={}, from={}, size={}", userId, from, size);
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        log.info("[getUserEventsPrivate] Retrieved {} events for userId={}", events.size(), userId);
        return eventMapper.mapToShortDto(events);
    }

    @Override
    @Transactional
    public EventFullDto addNewEventPrivate(NewEventDto dto, Long userId) {
        log.info("[addNewEventPrivate] User {} attempts to create a new event: {}", userId, dto.getTitle());
        checkEventDate(dto.getEventDate());

        Long catId = dto.getCategory();
        Category category = findCategoryOrThrow(catId);
        User user = findUserOrThrow(userId);

        Location location = locationRepository.save(dto.getLocation());

        Event event = eventMapper.mapToEvent(dto, user, category);
        event.setConfirmedRequests(0);
        event.setCreatedOn(LocalDateTime.now());
        event.setLocation(location);
        event.setViews(0L);
        event.setState(State.PENDING);
        Event save = eventRepository.save(event);

        log.info("[addNewEventPrivate] Event with id {} successfully created by user {}", save.getId(), userId);
        return eventMapper.mapToFullDto(save);
    }

    @Override
    public EventFullDto getEventByUserPrivate(Long userId, Long eventId) {
        log.info("[getEventByUserPrivate] User {} requests event with id {}", userId, eventId);
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException(Event.class, eventId));

        return eventMapper.mapToFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest updateDto) {
        log.info("[updateEventPrivate] User {} attempts to update event {} with data: {}", userId, eventId, updateDto);

        if (updateDto.getEventDate() != null) {
            checkEventDate(updateDto.getEventDate());
        }
        Event event = findEventOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            log.warn("[updateEventPrivate] User {} is not the initiator of event {}", userId, eventId);
            throw new ConflictException("User is not the initiator of the event.");
        }

        checkEventStatePrivate(event.getState());

        if (updateDto.getStateAction() != null) {
            switch (updateDto.getStateAction()) {
                case CANCEL_REVIEW -> {
                    event.setState(State.CANCELED);
                    log.info("[updateEventPrivate] Event {} state set to CANCELED", eventId);
                }
                case SEND_TO_REVIEW -> {
                    event.setState(State.PENDING);
                    log.info("[updateEventPrivate] Event {} state set to PENDING", eventId);
                }
                default -> {
                    log.warn("[updateEventPrivate] Invalid state action {} for event {}",
                            updateDto.getStateAction(), eventId);
                    throw new ConflictException("Invalid state action for private update.");
                }
            }
        }

        Category category = null;
        if (updateDto.getCategory() != null) {
            category = findCategoryOrThrow(updateDto.getCategory());
            log.info("[updateEventPrivate] Event {} category updated to {}", eventId, category.getId());
        }

        eventMapper.updateEvent(event, updateDto, category);
        eventRepository.save(event);

        log.info("[updateEventPrivate] Event {} successfully updated by user {}", eventId, userId);
        return eventMapper.mapToFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getUserEventRequestsPrivate(Long userId, Long eventId) {
        log.info("[getUserEventRequestsPrivate] User {} requests participant list for event {}", userId, eventId);
        User user = findUserOrThrow(userId);
        Event event = findEventOrThrow(eventId);

        if (!event.getInitiator().equals(user)) {
            log.warn("[getUserEventRequestsPrivate] User {} is not the initiator of event {}", user, event);
            throw new ValidationException("User is not the initiator of the event.");
        }

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);
        log.info("[getUserEventRequestsPrivate] Found {} requests for event {}", requests.size(), eventId);

        return requests.stream().map(requestMapper::toDto).toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestsPrivate(Long userId, Long eventId,
                                                                     EventRequestStatusUpdateRequest dto) {
        log.info("[updateEventRequestsPrivate] User {} attempts to update requests for event {} with status {}",
                userId, eventId, dto.getStatus());
        User user = findUserOrThrow(userId);
        Event event = findEventOrThrow(eventId);


        if (!event.getInitiator().equals(user)) {
            log.warn("[updateEventRequestsPrivate] User {} is not the initiator of event {}", userId, eventId);
            throw new ValidationException("Only the event initiator can update requests.");
        }

        Integer confirmedRequests = event.getConfirmedRequests();
        Integer limit = event.getParticipantLimit();
        if (dto.getStatus().equals(RequestStatus.CONFIRMED) && limit <= confirmedRequests) {
            throw new ConflictException("The participant limit has been reached.");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Event is not published.");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(dto.getRequestIds());

        for (ParticipationRequest participationRequest : requests) {
            if (!participationRequest.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Request must have status PENDING.");
            }

            if (confirmedRequests < limit && dto.getStatus().equals(RequestStatus.CONFIRMED)) {
                participationRequest.setStatus(RequestStatus.CONFIRMED);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            } else {
                participationRequest.setStatus(RequestStatus.REJECTED);
            }
        }
        eventRepository.save(event);
        List<ParticipationRequest> participationRequests = requestRepository.saveAll(requests);

        List<ParticipationRequestDto> confirmed = participationRequests.stream()
                .filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED))
                .map(requestMapper::toDto)
                .toList();
        List<ParticipationRequestDto> rejected = participationRequests.stream()
                .filter(request -> request.getStatus().equals(RequestStatus.REJECTED))
                .map(requestMapper::toDto)
                .toList();

        log.info("[updateEventRequestsPrivate] Confirmed: {}, Rejected: {}",
                confirmed.size(), rejected.size());

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected).build();
    }

    @Override
    public List<EventFullDto> getEventsAdmin(List<Long> users,
                                             List<String> states,
                                             List<Long> categories,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd,
                                             Integer from,
                                             Integer size) {
        log.info("[getEventsAdmin] Filter by users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        Specification<Event> specification = EventSpecification
                .adminFilterBuild(users, states, categories, rangeStart, rangeEnd);

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        log.info("[getEventsAdmin] Found {} events", events.size());
        return events.stream()
                .map(eventMapper::mapToFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest dto) {
        log.info("[updateEventAdmin] Updating event {} with data: {}", eventId, dto);

        if (dto.getEventDate() != null) {
            checkEventDate(dto.getEventDate());
        }
        Event event = findEventOrThrow(eventId);

        if (dto.getStateAction() != null) {
            checkEventStateAdmin(event.getState(), dto.getStateAction());
            switch (dto.getStateAction()) {
                case PUBLISH_EVENT -> {
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    log.info("Event {} published at {}", eventId, event.getPublishedOn());
                }
                case REJECT_EVENT -> {
                    event.setState(State.REJECTED);
                    log.info("Event {} rejected", eventId);
                }
                default -> throw new ValidationException("Cannot publish the event because " +
                        "it's not in the right state: " + event.getState());
            }
        }

        if (dto.getCategory() != null) {
            Category category = findCategoryOrThrow(dto.getCategory());
            event.setCategory(category);
            log.info("Event {} category updated to {}", eventId, category.getName());
        }

        eventMapper.updateEventAdmin(event, dto);
        locationRepository.save(event.getLocation());
        eventRepository.save(event);
        log.info("Event {} successfully updated", eventId);
        return eventMapper.mapToFullDto(event);
    }

    @Override
    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                               Integer size, HttpServletRequest httpServletRequest) {
        log.info("[getEventsPublic] Request received with params: text='{}', categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort='{}', from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            log.warn("[getEventsPublic] Validation failed: rangeStart {} is after rangeEnd {}", rangeStart, rangeEnd);
            throw new ValidationException("Range start can't be after range end.");
        }

        Specification<Event> specification = EventSpecification
                .publicFilterBuild(text, categories, paid, rangeStart, rangeEnd, onlyAvailable);

        Sort sorting = sorting(sort);

        hit(httpServletRequest);
        log.info("[getEventsPublic] Recorded hit for request URI: {}", httpServletRequest.getRequestURI());

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, sorting);
        List<Event> events = eventRepository.findAll(specification, pageable).getContent();
        log.info("[getEventsPublic] Retrieved {} events from repository", events.size());

        return events.stream()
                .map(eventMapper::mapToShortDto)
                .toList();
    }

    @Override
    public EventFullDto getEventByIdPublic(Long eventId, HttpServletRequest httpServletRequest) {
        log.info("[getEventByIdPublic] Fetching event with id {}", eventId);
        Event event = eventRepository.findById(eventId).filter(e -> e.getState().equals(State.PUBLISHED))
                .orElseThrow(() -> new NotFoundException(Event.class, eventId));

        hit(httpServletRequest);
        log.info("[getEventByIdPublic] Recorded hit for request URI: {}", httpServletRequest.getRequestURI());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        List<EndpointStatsResponse> stats = statsClient.findStats(event.getPublishedOn(), LocalDateTime.now(),
                List.of("/events/" + eventId), true);

        Long views = stats.isEmpty() ? 0L : stats.getFirst().getHits();
        event.setViews(views);
        log.info("[getEventByIdPublic] Updated event views: {}", views);


        return eventMapper.mapToFullDto(event);
    }

    private void hit(HttpServletRequest httpServletRequest) {
        EndpointHitRequest endpointHitRequest = new EndpointHitRequest(
                "main-server",
                httpServletRequest.getRequestURI(),
                httpServletRequest.getRemoteAddr(),
                LocalDateTime.now());
        statsClient.hit(endpointHitRequest);
    }

    private Sort sorting(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.unsorted();
        }
        SortOpt eventSortOpt = SortOpt.valueOf(sort.toUpperCase());
        return switch (eventSortOpt) {
            case EVENT_DATE -> Sort.by(Sort.Direction.DESC, "eventDate");
            case VIEWS -> Sort.by(Sort.Direction.DESC, "views");
        };
    }

    private void checkEventDate(String date) {
        String string = date.replace('T', ' ');
        if (LocalDateTime.parse(string, FORMATTER)
                .isBefore(LocalDateTime.now().plusHours(2))) {
            log.warn("[checkEventDate] Validation failed: eventDateTime {} is less than 2 hours from now", string);
            throw new ValidationException("The event date and time must be at least two hours in the future.");
        }
    }

    private Category findCategoryOrThrow(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(Category.class, catId));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(User.class, userId));
    }

    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(Event.class, eventId));
    }

    private void checkEventStatePrivate(State state) {
        if (state.equals(State.PUBLISHED)) {
            log.warn("[checkEventStatePrivate] Conflict: event state is PUBLISHED, update forbidden");
            throw new ConflictException("Only events in the CANCELLED or PENDING state can be updated.");
        }
    }

    private void checkEventStateAdmin(State currentState, StateAction action) {
        if (action == StateAction.PUBLISH_EVENT && currentState != State.PENDING) {
            log.warn("[checkEventStateAdmin] Conflict: trying to publish event in state {}", currentState);
            throw new ConflictException("Cannot publish the event because it's not in the right state: " + currentState);
        }

        if (action == StateAction.REJECT_EVENT && currentState == State.PUBLISHED) {
            log.warn("[checkEventStateAdmin] Conflict: trying to reject already published event");
            throw new ConflictException("Cannot reject the event because it has already been published.");
        }
    }


}
