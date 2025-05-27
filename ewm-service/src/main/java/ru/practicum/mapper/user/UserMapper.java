package ru.practicum.mapper.user;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.request.NewUserRequest;
import ru.practicum.dto.response.UserDto;
import ru.practicum.model.User;

import java.util.List;

@Mapper
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest userRequest);

    List<UserDto> mapToUserDto(Iterable<User> users);
}
