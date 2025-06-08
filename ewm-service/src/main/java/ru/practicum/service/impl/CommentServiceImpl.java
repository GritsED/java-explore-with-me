package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.CommentSearchParamsAdmin;
import ru.practicum.dto.request.NewCommentDto;
import ru.practicum.dto.response.CommentDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.model.enums.SortCommentOpt;
import ru.practicum.model.enums.State;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.repository.specification.CommentSpecification;
import ru.practicum.service.interfaces.CommentService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public CommentDto createCommentPrivate(Long userId, Long eventId, NewCommentDto newComment) {
        Result result = validateAndLoadUserAndEvent(userId, eventId);

        Comment comment = commentMapper.toEntity(newComment);
        comment.setAuthor(result.user());
        comment.setEvent(result.event());

        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateCommentPrivate(Long userId, Long eventId, Long commentId, NewCommentDto updatedComment) {
        validateAndLoadUserAndEvent(userId, eventId);

        Comment comment = getCommentOrThrow(commentId);

        checkCommentAuthor(userId, comment);

        if (comment.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(20))) {
            throw new ConflictException("Comment can be edited only within 20 minutes after creation");
        }

        comment.setText(updatedComment.getText());
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteCommentPrivate(Long userId, Long eventId, Long commentId) {
        validateAndLoadUserAndEvent(userId, eventId);
        Comment comment = getCommentOrThrow(commentId);
        checkCommentAuthor(userId, comment);

        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getAllUserCommentsPrivate(Long userId, Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(User.class, userId));
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Comment> comments = commentRepository.findAllByAuthorId(userId, pageable).getContent();

        return comments.stream().map(commentMapper::toDto).toList();
    }

    @Override
    public CommentDto getCommentPublic(Long commentId) {
        Comment comment = getCommentOrThrow(commentId);
        return commentMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getAllUserCommentsAdmin(CommentSearchParamsAdmin params, Integer from, Integer size) {
        Specification<Comment> commentSpecification = CommentSpecification.adminFilterBuild(params);
        SortCommentOpt sort = params.getSort();

        Sort sorting = sorting(sort);
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, sorting);

        List<Comment> comments = commentRepository.findAll(commentSpecification, pageable).getContent();
        return comments.stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(Long commentId) {
        Comment comment = getCommentOrThrow(commentId);

        commentRepository.delete(comment);
    }

    private Sort sorting(SortCommentOpt sort) {
        if (sort == null) {
            return Sort.unsorted();
        }
        return switch (sort) {
            case CREATED_ASC -> Sort.by(Sort.Direction.ASC, "createdAt");
            case CREATED_DESC -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private void checkCommentAuthor(Long userId, Comment comment) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("User with id " + userId + " isn't the author of the comment with id " + comment.getId());
        }
    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException(Comment.class, commentId));
    }

    private Result validateAndLoadUserAndEvent(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(User.class, userId));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(Event.class, eventId));

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Event must be published to interact with comments");
        }
        return new Result(user, event);
    }

    private record Result(User user, Event event) {
    }
}
