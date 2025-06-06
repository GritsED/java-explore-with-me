package ru.practicum.service.interfaces;

import ru.practicum.dto.request.NewCompilationDto;
import ru.practicum.dto.request.UpdateCompilationRequest;
import ru.practicum.dto.response.CompilationDto;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilationAdmin(NewCompilationDto newCompilationDto);

    void deleteCompilationAdmin(Long id);

    CompilationDto updateCompAdmin(Long id, UpdateCompilationRequest updateCompilationRequest);

    List<CompilationDto> getCompilationsPublic(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationPublic(Long compId);
}
