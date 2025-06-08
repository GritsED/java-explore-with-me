package ru.practicum.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.dto.request.CommentSearchParamsAdmin;
import ru.practicum.model.Comment;
import ru.practicum.model.enums.SortCommentOpt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentSpecification {
    public static Specification<Comment> adminFilterBuild(CommentSearchParamsAdmin params) {
        LocalDateTime rangeStart = params.getRangeStart();
        LocalDateTime rangeEnd = params.getRangeEnd();
        List<Long> eventIds = params.getEventIds();
        List<Long> userIds = params.getUserIds();
        SortCommentOpt sort = params.getSort();

        return (((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (rangeStart != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), rangeStart));
            }

            if (rangeEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), rangeEnd));
            }

            if (eventIds != null && !eventIds.isEmpty()) {
                predicates.add(root.get("event").get("id").in(eventIds));
            }

            if (userIds != null && !userIds.isEmpty()) {
                predicates.add(root.get("author").get("id").in(userIds));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }));
    }
}
