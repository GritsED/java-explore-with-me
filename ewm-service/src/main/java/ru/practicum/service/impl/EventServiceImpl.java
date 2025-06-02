package ru.practicum.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;

    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<EventShortDto> getUserEventsPrivate(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        return eventMapper.mapToShortDto(events);
    }

    @Override
    @Transactional
    public EventFullDto addNewEventPrivate(NewEventDto dto, Long userId) {
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

        return eventMapper.mapToFullDto(save);
    }

    @Override
    public EventFullDto getEventByUserPrivate(Long userId, Long eventId) {
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found."));

        return eventMapper.mapToFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest updateDto) {
        Category category = null;

        if (updateDto.getEventDate() != null) {
            checkEventDate(updateDto.getEventDate());
        }
        Event event = findEventOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User is not the initiator of the event.");
        }

        checkEventStatePrivate(event.getState());

        if (updateDto.getStateAction() != null) {
            switch (updateDto.getStateAction()) {
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                default -> throw new ConflictException("Invalid state action for private update.");
            }
        }

        if (updateDto.getCategory() != null) {
            category = findCategoryOrThrow(updateDto.getCategory());
        }

        eventMapper.updateEvent(event, updateDto, category);
        eventRepository.save(event);
        return eventMapper.mapToFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getUserEventRequestsPrivate(Long userId, Long eventId) {
        User user = findUserOrThrow(userId);
        Event event = findEventOrThrow(eventId);

        if (!event.getInitiator().equals(user)) {
            throw new ValidationException("User is not the initiator of the event.");
        }

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);

        return requests.stream().map(requestMapper::toDto).toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestsPrivate(Long userId, Long eventId,
                                                                     EventRequestStatusUpdateRequest dto) {
        User user = findUserOrThrow(userId);
        Event event = findEventOrThrow(eventId);


        if (!event.getInitiator().equals(user)) {
            throw new ValidationException("Only the event initiator can update requests.");
        }

        Integer confirmedRequests = event.getConfirmedRequests();
        Integer limit = event.getParticipantLimit();
        if (dto.getStatus().equals(RequestStatus.CONFIRMED) && limit <= confirmedRequests) {
            throw new ConflictException("Participant limit exceeded.");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Event is not published.");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(dto.getRequestIds());

        for (ParticipationRequest participationRequest : requests) {
            if (!participationRequest.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Request status is not PENDING.");
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
        Specification<Event> specification = EventSpecification
                .adminFilterBuild(users, states, categories, rangeStart, rangeEnd);

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        return events.stream()
                .map(eventMapper::mapToFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Category category = null;

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
                }
                case REJECT_EVENT -> event.setState(State.REJECTED);
                default -> throw new ConflictException("Invalid state action for admin update.");
            }
        }

        if (dto.getCategory() != null) {
            category = findCategoryOrThrow(dto.getCategory());
            event.setCategory(category);
        }

        eventMapper.updateEventAdmin(event, dto);
        locationRepository.save(event.getLocation());
        eventRepository.save(event);
        return eventMapper.mapToFullDto(event);
    }

    @Override
    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                               Integer size, HttpServletRequest httpServletRequest) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Range start can't be after range end.");
        }

        Specification<Event> specification = EventSpecification
                .publicFilterBuild(text, categories, paid, rangeStart, rangeEnd, onlyAvailable);

        Sort sorting = sorting(sort);

        hit(httpServletRequest);

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, sorting);
        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        return events.stream()
                .map(eventMapper::mapToShortDto)
                .toList();
    }

    @Override
    public EventFullDto getEventByIdPublic(Long eventId, HttpServletRequest httpServletRequest) {
        Event event = eventRepository.findById(eventId).filter(e -> e.getState().equals(State.PUBLISHED))
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found."));

        hit(httpServletRequest);

        List<EndpointStatsResponse> stats = statsClient.findStats(event.getPublishedOn(), LocalDateTime.now(),
                List.of("/events/" + eventId), true);

        Long views = stats.isEmpty() ? 0L : stats.getFirst().getHits();
        event.setViews(views);

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
            throw new ValidationException("The event date and time must be at least two hours in the future.");
        }
    }

    private Category findCategoryOrThrow(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id " + catId + " not found."));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found."));
    }

    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found."));
    }

    private void checkEventStatePrivate(State state) {
        if (state.equals(State.PUBLISHED)) {
            throw new ConflictException("Only events in the CANCELLED or PENDING state can be updated.");
        }
    }

    private void checkEventStateAdmin(State currentState, StateAction action) {
        if (action == StateAction.PUBLISH_EVENT && currentState != State.PENDING) {
            throw new ConflictException("Cannot publish the event because it's not in the right state: " + currentState);
        }

        if (action == StateAction.REJECT_EVENT && currentState == State.PUBLISHED) {
            throw new ConflictException("Cannot reject the event because it has already been published.");
        }
    }


}
