package ru.practicum.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventSearchParamsPublic extends EventSearchParamsAdmin {
    String text;

    Boolean paid;

    Boolean onlyAvailable = false;

    String sortOpt;
}
