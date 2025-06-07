package ru.practicum.service.interfaces;

import ru.practicum.dto.request.NewCommentDto;
import ru.practicum.dto.response.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, Long eventId, NewCommentDto comment);

    CommentDto updateComment(Long userId, Long eventId, Long commentId, NewCommentDto comment);

    void deleteComment(Long userId, Long eventId, Long commentId);

    List<CommentDto> getAllUserComments(Long userId, Integer from, Integer size);
}
