package com.univer.bookcom.service;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.dto.request.CommentsRequestDto;
import com.univer.bookcom.dto.response.CommentsResponseDto;
import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.CommentNotFoundException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.mapper.DtoMapper;
import com.univer.bookcom.model.Comments;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.CommentsRepository;
import com.univer.bookcom.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentsService {
    private static final Logger log = LoggerFactory.getLogger(CommentsService.class);
    private static final String NOT_FOUND_MESSAGE = " не найден";
    private static final String BOOK_NOT_FOUND = "Книга с id ";
    private static final String USER_NOT_FOUND = "Пользователь с id ";
    private static final String COMMENT_NOT_FOUND = "Комментарий с id ";

    private final CommentsRepository commentsRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CacheContainer cacheContainer;
    private final DtoMapper dtoMapper;

    public CommentsService(CommentsRepository commentsRepository,
                           BookRepository bookRepository,
                           UserRepository userRepository,
                           CacheContainer cacheContainer,
                           DtoMapper dtoMapper) {
        this.commentsRepository = commentsRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.cacheContainer = cacheContainer;
        this.dtoMapper = dtoMapper;
    }

    public CommentsResponseDto createComment(CommentsRequestDto commentDto) {
        Comments comment = dtoMapper.toCommentsEntity(commentDto);
        Comments saved = commentsRepository.save(comment);
        cacheContainer.getCommentsCache().put(saved.getId(), new CacheEntry<>(saved));
        log.debug("Создан новый комментарий с ID: {}", saved.getId());
        return dtoMapper.toCommentsResponseDto(saved);
    }

    public List<CommentsResponseDto> getCommentsByBookId(Long bookId) {
        List<Comments> comments = commentsRepository.findByBookId(bookId);
        log.debug("Найдено {} комментариев для книги с ID: {}", comments.size(), bookId);
        return comments.stream()
                .map(dtoMapper::toCommentsResponseDto)
                .collect(Collectors.toList());
    }

    public List<CommentsResponseDto> getCommentsByUserId(Long userId) {
        List<Comments> comments = commentsRepository.findByUserId(userId);
        log.debug("Найдено {} комментариев пользователя с ID: {}", comments.size(), userId);
        return comments.stream()
                .map(dtoMapper::toCommentsResponseDto)
                .collect(Collectors.toList());
    }

    public CommentsResponseDto updateComment(Long commentId, CommentsRequestDto commentDto) {
        Comments comment = commentsRepository.findById(commentId).orElseThrow(() ->
                new CommentNotFoundException(COMMENT_NOT_FOUND + commentId + NOT_FOUND_MESSAGE));
        comment.setText(commentDto.getText());
        Comments updated = commentsRepository.save(comment);
        cacheContainer.getCommentsCache().put(commentId, new CacheEntry<>(updated));
        log.debug("Обновлен комментарий с ID: {}", commentId);
        return dtoMapper.toCommentsResponseDto(updated);
    }

    public void deleteComment(Long commentId) {
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(
                        COMMENT_NOT_FOUND + commentId + NOT_FOUND_MESSAGE));

        commentsRepository.delete(comment);
        cacheContainer.getCommentsCache().remove(commentId);
        log.debug("Комментарий с ID: {} удален", commentId);
    }
}