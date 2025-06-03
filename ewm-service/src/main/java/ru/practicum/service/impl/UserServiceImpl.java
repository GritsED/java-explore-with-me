package ru.practicum.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.NewUserRequest;
import ru.practicum.dto.response.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.interfaces.UserService;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> findUsers(List<Long> ids, Integer from, Integer size) {
        log.info("[findUsers] Called with ids: {}, from: {}, size: {}", ids, from, size);
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        List<User> users = (ids == null || ids.isEmpty()) ?
                userRepository.findAll(pageable).getContent() :
                userRepository.findByIdIn(ids, pageable).getContent();

        log.info("[findUsers] Found {} users", users.size());
        return userMapper.mapToUserDto(users);
    }

    @Override
    @Transactional
    public UserDto addUser(NewUserRequest newUser) {
        log.info("[addUser] Attempt to add user: {}", newUser);
        String email = newUser.getEmail();
        if (userRepository.existsByEmail(email)) {
            log.warn("[addUser] User with email {} already exists", email);
            throw new ConflictException("User with email " + email + " already exists");
        }

        User user = userRepository.save(userMapper.toEntity(newUser));
        log.info("[addUser] User created with id: {}", user.getId());
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("[deleteUser] Attempt to delete user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(User.class, id));

        userRepository.delete(user);
        log.info("[deleteUser] User with id {} successfully deleted", id);
    }
}
