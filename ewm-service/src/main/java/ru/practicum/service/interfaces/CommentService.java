package ru.practicum.service.interfaces;

import ru.practicum.dto.request.CommentSearchParamsAdmin;
import ru.practicum.dto.request.NewCommentDto;
import ru.practicum.dto.response.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto createCommentPrivate(Long userId, Long eventId, NewCommentDto comment);

    CommentDto updateCommentPrivate(Long userId, Long eventId, Long commentId, NewCommentDto comment);

    void deleteCommentPrivate(Long userId, Long eventId, Long commentId);

    List<CommentDto> getAllUserCommentsPrivate(Long userId, Integer from, Integer size);

    CommentDto getCommentPublic(Long commentId);

    List<CommentDto> getAllUserCommentsAdmin(CommentSearchParamsAdmin params, Integer from, Integer size);

    void deleteCommentAdmin(Long commentId);
}
