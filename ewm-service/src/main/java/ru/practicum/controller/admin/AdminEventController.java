package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.EventSearchParamsAdmin;
import ru.practicum.dto.request.UpdateEventAdminRequest;
import ru.practicum.dto.response.EventFullDto;
import ru.practicum.service.interfaces.EventService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public List<EventFullDto> getEvents(@ModelAttribute EventSearchParamsAdmin params,
                                        @RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getEventsAdmin(params, from, size);
    }

    @PatchMapping("{eventId}")
    @ResponseStatus(value = HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventAdminRequest eventAdminRequest) {
        return eventService.updateEventAdmin(eventId, eventAdminRequest);
    }
}
