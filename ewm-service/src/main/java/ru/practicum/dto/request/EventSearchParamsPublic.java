package ru.practicum.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventSearchParamsPublic extends EventSearchParamsAdmin {
    String text;

    Boolean paid;

    Boolean onlyAvailable = false;

    String sortOpt;
}
