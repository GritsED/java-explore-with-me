package ru.practicum.controller.publ;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.EventSearchParamsPublic;
import ru.practicum.dto.response.EventFullDto;
import ru.practicum.dto.response.EventShortDto;
import ru.practicum.service.interfaces.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(@ModelAttribute EventSearchParamsPublic params,
                                         @RequestParam(defaultValue = "0") Integer from,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         HttpServletRequest httpServletRequest) {
        return eventService.getEventsPublic(params, from, size, httpServletRequest);
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        return eventService.getEventByIdPublic(id, httpServletRequest);
    }
}
