package com.univer.bookcom.service;

import com.univer.bookcom.cache.CacheContainer;
import com.univer.bookcom.cache.CacheEntry;
import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.CommentNotFoundException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.Comments;
import com.univer.bookcom.model.User;
import com.univer.bookcom.model.dto.request.CommentsRequestDto;
import com.univer.bookcom.model.dto.response.CommentsResponseDto;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.CommentsRepository;
import com.univer.bookcom.repository.UserRepository;
import com.univer.bookcom.service.mapper.CommentsMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    private final CommentsMapper commentsMapper;

    public CommentsService(CommentsRepository commentsRepository,
                           BookRepository bookRepository,
                           UserRepository userRepository,
                           CacheContainer cacheContainer,
                           CommentsMapper commentsMapper) {
        this.commentsRepository = commentsRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.cacheContainer = cacheContainer;
        this.commentsMapper = commentsMapper;
    }

    @Transactional
    public CommentsResponseDto createCommentDto(CommentsRequestDto commentDto) {
        Book book = bookRepository.findById(commentDto.getBookId()).orElseThrow(() ->
                new BookNotFoundException(BOOK_NOT_FOUND + commentDto
                        .getBookId() + NOT_FOUND_MESSAGE));
        User user = userRepository.findById(commentDto.getUserId()).orElseThrow(() ->
                new UserNotFoundException(USER_NOT_FOUND + commentDto
                        .getUserId() + NOT_FOUND_MESSAGE));

        Comments comment = commentsMapper.toEntity(commentDto);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setBook(book);
        comment.setUser(user);

        Comments saved = commentsRepository.save(comment);
        cacheContainer.getCommentsCache().put(saved.getId(), new CacheEntry<>(saved));

        Hibernate.initialize(book.getComments());
        Hibernate.initialize(book.getAuthors());
        cacheContainer.getBookCache().put(book.getId(), new CacheEntry<>(book));
        log.debug("Кэш книги ID {} обновлен после добавления комментария ID: {}",
                book.getId(), saved.getId());

        Hibernate.initialize(user.getBooks());
        Hibernate.initialize(user.getComments());
        cacheContainer.getUserCache().put(user.getId(), new CacheEntry<>(user));
        log.debug("Кэш пользователя ID {} обновлен после добавления комментария ID: {}",
                user.getId(), saved.getId());

        log.debug("Создан новый комментарий с ID: {}", saved.getId());
        return commentsMapper.toResponseDto(saved);
    }

    @Transactional
    public List<CommentsResponseDto> getCommentsByBookIdDto(Long bookId) {
        List<Comments> comments = commentsRepository.findByBookId(bookId);
        log.debug("Найдено {} комментариев для книги с ID: {}", comments.size(), bookId);
        return comments.stream()
                .map(commentsMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<CommentsResponseDto> getCommentsByUserIdDto(Long userId) {
        List<Comments> comments = commentsRepository.findByUserId(userId);
        log.debug("Найдено {} комментариев пользователя с ID: {}", comments.size(), userId);
        return comments.stream()
                .map(commentsMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentsResponseDto updateCommentDto(Long commentId, CommentsRequestDto commentDto) {
        Comments comment = commentsRepository.findById(commentId).orElseThrow(() ->
                new CommentNotFoundException(COMMENT_NOT_FOUND + commentId + NOT_FOUND_MESSAGE));

        if (!comment.getUser().getId().equals(commentDto.getUserId())) {
            log.warn("Попытка обновить комментарий ID {} пользователем ID {}, "
                            + "не являющимся автором (автор ID {})",
                    commentId, commentDto.getUserId(), comment.getUser().getId());
            throw new IllegalArgumentException("Вы не можете редактировать чужой комментарий");
        }

        comment.setText(commentDto.getText());
        Comments updated = commentsRepository.save(comment);
        cacheContainer.getCommentsCache().put(commentId, new CacheEntry<>(updated));
        log.debug("Обновлен комментарий с ID: {}", commentId);
        return commentsMapper.toResponseDto(updated);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(
                        COMMENT_NOT_FOUND + commentId + NOT_FOUND_MESSAGE));

        Hibernate.initialize(comment.getBook());
        Hibernate.initialize(comment.getUser());

        Book book = comment.getBook();
        User user = comment.getUser();

        if (book != null) {
            Hibernate.initialize(book.getComments());
            boolean removed = book.getComments().remove(comment);
            log.debug("Комментарий ID {} {} из коллекции comments книги ID {}",
                    commentId, removed ? "удален" : "не найден", book.getId());
            bookRepository.save(book);
        }

        if (user != null) {
            Hibernate.initialize(user.getComments());
            boolean removed = user.getComments().remove(comment);
            log.debug("Комментарий ID {} {} из коллекции comments пользователя ID {}",
                    commentId, removed ? "удален" : "не найден", user.getId());
            userRepository.save(user);
        }

        commentsRepository.delete(comment);
        commentsRepository.flush();
        log.debug("Комментарий ID {} удален из базы данных", commentId);

        if (commentsRepository.existsById(commentId)) {
            log.error("Комментарий ID {} не был удалён из базы данных", commentId);
            throw new RuntimeException("Не удалось удалить комментарий ID: " + commentId);
        }

        cacheContainer.getCommentsCache().remove(commentId);

        if (book != null) {
            Hibernate.initialize(book.getComments());
            Hibernate.initialize(book.getAuthors());
            cacheContainer.getBookCache().put(book.getId(), new CacheEntry<>(book));
            log.debug("Кэш книги ID {} обновлен после удаления комментария ID {}",
                    book.getId(), commentId);
        }

        if (user != null) {
            Hibernate.initialize(user.getBooks());
            Hibernate.initialize(user.getComments());
            cacheContainer.getUserCache().put(user.getId(), new CacheEntry<>(user));
            log.debug("Кэш пользователя ID {} обновлен после удаления комментария ID {}",
                    user.getId(), commentId);
        }

        log.info("Комментарий с ID: {} успешно удален", commentId);
    }

    @Transactional
    public Comments createComment(Long bookId, Long userId, String text) {
        Book book = bookRepository.findById(bookId).orElseThrow(() ->
                new BookNotFoundException(BOOK_NOT_FOUND + bookId + NOT_FOUND_MESSAGE));
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(USER_NOT_FOUND + userId + NOT_FOUND_MESSAGE));

        Comments comment = new Comments();
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setBook(book);
        comment.setUser(user);

        Comments saved = commentsRepository.save(comment);
        cacheContainer.getCommentsCache().put(saved.getId(), new CacheEntry<>(saved));
        log.debug("Создан новый комментарий с ID: {}", saved.getId());
        return saved;
    }
}