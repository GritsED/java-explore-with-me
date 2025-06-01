package ru.practicum.service.interfaces;

import ru.practicum.dto.request.NewCompilationDto;
import ru.practicum.dto.request.UpdateCompilationRequest;
import ru.practicum.dto.response.CompilationDto;

public interface CompilationService {
    CompilationDto addCompilationAdmin(NewCompilationDto newCompilationDto);

    void deleteCompilationAdmin(Long id);

    CompilationDto updateCompAdmin(Long id, UpdateCompilationRequest updateCompilationRequest);
}
