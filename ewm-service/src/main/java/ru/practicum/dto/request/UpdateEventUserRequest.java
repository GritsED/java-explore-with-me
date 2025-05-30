package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.model.Location;
import ru.practicum.model.enums.StateAction;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {
    String annotation;
    Long category;
    String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    String eventDate;
    Location location;
    Boolean paid;
    @Min(0)
    Integer participantLimit;
    Boolean requestModeration;
    StateAction stateAction;
    String title;
}
