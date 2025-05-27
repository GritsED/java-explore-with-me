package ru.practicum.service;

import ru.practicum.dto.request.NewUserRequest;
import ru.practicum.dto.response.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> findUsers(List<Long> ids, Integer from, Integer size);

    UserDto addUser(NewUserRequest dto);

    void deleteUser(Long id);
}
