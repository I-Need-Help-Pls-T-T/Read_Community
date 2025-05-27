package com.univer.bookcom.mapper;

import com.univer.bookcom.dto.request.BookRequestDto;
import com.univer.bookcom.dto.request.CommentsRequestDto;
import com.univer.bookcom.dto.request.UserRequestDto;
import com.univer.bookcom.dto.response.BookResponseDto;
import com.univer.bookcom.dto.response.CommentsResponseDto;
import com.univer.bookcom.dto.response.UserResponseDto;
import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.Comments;
import com.univer.bookcom.model.User;
import com.univer.bookcom.repository.BookRepository;
import com.univer.bookcom.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public DtoMapper(UserRepository userRepository, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public User toUserEntity(UserRequestDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }

    public UserResponseDto toUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setBookIds(user.getBooks() != null
                ? user.getBooks().stream().map(Book::getId).collect(Collectors.toList())
                : Collections.emptyList());
        dto.setCommentIds(user.getComments() != null
                ? user.getComments().stream().map(Comments::getId).collect(Collectors.toList())
                : Collections.emptyList());
        return dto;
    }

    public Book toBookEntity(BookRequestDto dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setCountChapters(dto.getCountChapters());
        book.setPublicYear(dto.getPublicYear());
        book.setBookStatus(dto.getStatus());
        if (dto.getAuthorIds() != null && !dto.getAuthorIds().isEmpty()) {
            List<User> authors = dto.getAuthorIds().stream()
                    .map(id -> userRepository.findById(id)
                            .orElseThrow(() -> new UserNotFoundException("Автор с id " + id + " не найден")))
                    .collect(Collectors.toList());
            book.setAuthors(authors);
        }
        return book;
    }

    public BookResponseDto toBookResponseDto(Book book) {
        BookResponseDto dto = new BookResponseDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setCountChapters(book.getCountChapters());
        dto.setPublicYear(book.getPublicYear());
        dto.setStatus(book.getBookStatus());
        dto.setAuthorIds(book.getAuthors() != null
                ? book.getAuthors().stream().map(User::getId).collect(Collectors.toList())
                : Collections.emptyList());
        dto.setCommentIds(book.getComments() != null
                ? book.getComments().stream().map(Comments::getId).collect(Collectors.toList())
                : Collections.emptyList());
        return dto;
    }

    public Comments toCommentsEntity(CommentsRequestDto dto) {
        Comments comment = new Comments();
        comment.setText(dto.getText());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUser(userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + dto.getUserId() + " не найден")));
        comment.setBook(bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Книга с id " + dto.getBookId() + " не найдена")));
        return comment;
    }

    public CommentsResponseDto toCommentsResponseDto(Comments comment) {
        CommentsResponseDto dto = new CommentsResponseDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUserId(comment.getUser() != null ? comment.getUser().getId() : null);
        dto.setBookId(comment.getBook() != null ? comment.getBook().getId() : null);
        return dto;
    }
}