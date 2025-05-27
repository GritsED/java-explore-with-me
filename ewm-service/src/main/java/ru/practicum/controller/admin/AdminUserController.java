package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.NewUserRequest;
import ru.practicum.dto.response.UserDto;
import ru.practicum.service.interfaces.UserService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> findUsers(@RequestParam(required = false) List<Long> ids,
                                   @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                   @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /admin/users - find users: ids={}, from={}, size={}", ids, from, size);
        return userService.findUsers(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public UserDto addUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.info("POST /admin/users — add user: {}", newUserRequest);
        return userService.addUser(newUserRequest);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /admin/users/{}", userId);
        userService.deleteUser(userId);
    }
}
