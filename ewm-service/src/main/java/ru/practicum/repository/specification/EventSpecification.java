package ru.practicum.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventSpecification {
    public static Specification<Event> adminFilterBuild(List<Long> users,
                                                        List<String> states,
                                                        List<Long> categories,
                                                        String rangeStart,
                                                        String rangeEnd) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (users != null && !users.isEmpty()) {
                predicates.add(root.get("initiator").get("id").in(users));
            }

            if (states != null && !states.isEmpty()) {
                predicates.add(root.get("state").in(states));
            }

            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }

            if (rangeStart != null && !rangeStart.isEmpty()) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }

            if (rangeEnd != null && !rangeEnd.isEmpty()) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
