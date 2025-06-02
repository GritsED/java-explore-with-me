package ru.practicum.mapper;

import org.mapstruct.*;
import ru.practicum.dto.request.NewCompilationDto;
import ru.practicum.dto.request.UpdateCompilationRequest;
import ru.practicum.dto.response.CompilationDto;
import ru.practicum.dto.response.EventShortDto;
import ru.practicum.model.Compilation;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {EventMapper.class, UserMapper.class})
public interface CompilationMapper {
    @Mapping(source = "events", target = "events")
    @Mapping(target = "pinned", source = "compilationDto.pinned", defaultValue = "false")
    Compilation toEntity(NewCompilationDto compilationDto, List<EventShortDto> events);

    CompilationDto toDto(Compilation compilation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "events", target = "events")
    void updateCompilationAdmin(@MappingTarget Compilation compilation,
                                UpdateCompilationRequest updateCompilationRequest,
                                List<EventShortDto> events);

}
