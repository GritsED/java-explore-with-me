package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public CompilationDto addCompilationAdmin(NewCompilationDto newCompilationDto) {
        List<Long> events = newCompilationDto.getEvents();

        List<Event> eventList = eventRepository.findAllByIdIn(events);
        List<EventShortDto> list = eventList.stream().map(eventMapper::mapToShortDto).toList();
        Compilation entity = compilationMapper.toEntity(newCompilationDto, list);

        compilationRepository.save(entity);

        return compilationMapper.toDto(entity);
    }

    @Override
    public void deleteCompilationAdmin(Long id) {
        Compilation compilation = getCompOrThrow(id);

        compilationRepository.delete(compilation);
    }

    @Override
    public CompilationDto updateCompAdmin(Long id, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = getCompOrThrow(id);

        List<Long> events = updateCompilationRequest.getEvents();

        List<Event> eventList = eventRepository.findAllByIdIn(events);
        List<EventShortDto> list = eventList.stream().map(eventMapper::mapToShortDto).toList();

        compilationMapper.updateCompilationAdmin(compilation, updateCompilationRequest, list);

        return compilationMapper.toDto(compilation);
    }

    private Compilation getCompOrThrow(Long id) {
        return compilationRepository.findById(id).orElseThrow(() -> new NotFoundException("Compilation not found"));
    }
}
