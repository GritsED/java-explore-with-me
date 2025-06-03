package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.NewCompilationDto;
import ru.practicum.dto.request.UpdateCompilationRequest;
import ru.practicum.dto.response.CompilationDto;
import ru.practicum.dto.response.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.interfaces.CompilationService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public CompilationDto addCompilationAdmin(NewCompilationDto newCompilationDto) {
        log.info("[addCompilationAdmin] Admin attempts to add new compilation: {}", newCompilationDto);
        List<Long> events = newCompilationDto.getEvents();

        List<Event> eventList = eventRepository.findAllByIdIn(events);
        List<EventShortDto> list = eventList.stream().map(eventMapper::mapToShortDto).toList();
        Compilation entity = compilationMapper.toEntity(newCompilationDto, list);

        compilationRepository.save(entity);

        log.info("[addCompilationAdmin] New compilation successfully added with id: {}", entity.getId());
        return compilationMapper.toDto(entity);
    }

    @Override
    @Transactional
    public void deleteCompilationAdmin(Long id) {
        log.info("[deleteCompilationAdmin] Admin attempts to delete compilation with id {}", id);
        Compilation compilation = getCompOrThrow(id);

        compilationRepository.delete(compilation);
        log.info("[deleteCompilationAdmin] Compilation with id {} successfully deleted", id);
    }

    @Override
    @Transactional
    public CompilationDto updateCompAdmin(Long id, UpdateCompilationRequest updateCompilationRequest) {
        log.info("[updateCompAdmin] Admin attempts to update compilation with id {} using data: {}",
                id, updateCompilationRequest);
        Compilation compilation = getCompOrThrow(id);

        List<Long> events = updateCompilationRequest.getEvents();

        List<Event> eventList = eventRepository.findAllByIdIn(events);
        List<EventShortDto> list = eventList.stream().map(eventMapper::mapToShortDto).toList();

        compilationMapper.updateCompilationAdmin(compilation, updateCompilationRequest, list);

        log.info("[updateCompAdmin] Compilation with id {} successfully updated", id);
        return compilationMapper.toDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilationsPublic(Boolean pinned, Integer from, Integer size) {
        log.info("[getCompilationsPublic] Request with pinned={}, from={}, size={}", pinned, from, size);
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable).getContent();

        log.info("[getCompilationsPublic] Retrieved {} compilations", compilations.size());
        return compilations.stream()
                .map(compilationMapper::toDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationPublic(Long compId) {
        log.info("[getCompilationPublic] Public request for compilation with id {}", compId);
        return compilationMapper.toDto(getCompOrThrow(compId));
    }

    private Compilation getCompOrThrow(Long id) {
        return compilationRepository.findById(id).orElseThrow(() -> new NotFoundException(Compilation.class, id));
    }
}
