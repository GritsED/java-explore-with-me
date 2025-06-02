package ru.practicum.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(Class<?> className, Long entityId) {
        super(className.getSimpleName() + " with id " + entityId + " not found");
    }
}
