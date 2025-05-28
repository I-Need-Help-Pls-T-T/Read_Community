package com.univer.bookcom.service.mapper;

import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.Comments;
import com.univer.bookcom.model.User;
import com.univer.bookcom.model.dto.request.UserRequestDto;
import com.univer.bookcom.model.dto.response.UserResponseDto;
import com.univer.bookcom.repository.BookRepository;
import java.util.Collections;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final BookRepository bookRepository;

    public UserMapper(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public User toEntity(UserRequestDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }

    public UserResponseDto toResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        if (user.getBooks() != null && Hibernate.isInitialized(user.getBooks())) {
            dto.setBookTitles(user.getBooks().stream()
                    .filter(book -> bookRepository.existsById(book.getId()))
                    .map(Book::getTitle)
                    .collect(Collectors.toList()));
        } else {
            dto.setBookTitles(Collections.emptyList());
        }

        if (user.getComments() != null && Hibernate.isInitialized(user.getComments())) {
            dto.setCommentTexts(user.getComments().stream()
                    .map(Comments::getText)
                    .collect(Collectors.toList()));
        } else {
            dto.setCommentTexts(Collections.emptyList());
        }

        return dto;
    }
}