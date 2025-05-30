package ru.practicum.mapper;

import org.mapstruct.*;
import ru.practicum.dto.request.NewEventDto;
import ru.practicum.dto.request.UpdateEventAdminRequest;
import ru.practicum.dto.request.UpdateEventUserRequest;
import ru.practicum.dto.response.EventFullDto;
import ru.practicum.dto.response.EventShortDto;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;

import java.util.List;

@Mapper(uses = {UserMapper.class, CategoryMapper.class})
public interface EventMapper {
    List<EventShortDto> mapToShortDto(Iterable<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "newEventDto.eventDate", target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(source = "user", target = "initiator")
    @Mapping(source = "category", target = "category")
    Event mapToEvent(NewEventDto newEventDto, User user, Category category);

    @Mapping(source = "event.initiator", target = "initiator")
    EventShortDto mapToShortDto(Event event);

    @Mapping(source = "eventDate", target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(source = "createdOn", target = "createdOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(source = "publishedOn", target = "publishedOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(source = "event.initiator", target = "initiator")
    EventFullDto mapToFullDto(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", source = "category")
    void updateEvent(@MappingTarget Event event, UpdateEventUserRequest newEventDto, Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", source = "category")
    void updateEventAdmin(@MappingTarget Event event, UpdateEventAdminRequest newEventDto, Category category);

}
