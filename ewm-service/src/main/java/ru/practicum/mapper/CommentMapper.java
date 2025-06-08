package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.dto.request.NewCommentDto;
import ru.practicum.dto.response.CommentDto;
import ru.practicum.model.Comment;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class, EventMapper.class})
public interface CommentMapper {
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Comment toEntity(NewCommentDto newCommentDto);

    @Mapping(source = "comment.author", target = "author")
    @Mapping(source = "comment.event", target = "event")
    CommentDto toDto(Comment comment);
}
