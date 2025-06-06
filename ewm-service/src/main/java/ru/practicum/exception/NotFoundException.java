package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotFoundException extends RuntimeException {
    public NotFoundException(Class<?> className, Long entityId) {
        super(className.getSimpleName() + " with id " + entityId + " not found");
        log.warn("[NotFoundException] {} with id={} was not found", className.getSimpleName(), entityId);
    }
}
