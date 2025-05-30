package com.univer.bookcom.service.mapper;

import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.BookStatus;
import com.univer.bookcom.model.User;
import com.univer.bookcom.model.dto.request.BookRequestDto;
import com.univer.bookcom.model.dto.response.BookResponseDto;
import java.util.Collections;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book toEntity(BookRequestDto dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setCountChapters(dto.getCountChapters());
        book.setPublicYear(dto.getPublicYear());
        book.setDescription(dto.getDescription());
        book.setBookStatus(BookStatus.valueOf(dto.getBookStatus()));
        return book;
    }

    public BookResponseDto toResponseDto(Book book) {
        BookResponseDto dto = new BookResponseDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setCountChapters(book.getCountChapters());
        dto.setPublicYear(book.getPublicYear());
        dto.setDescription(book.getDescription());
        dto.setBookStatus(book.getBookStatus().name());

        if (book.getAuthors() != null && Hibernate.isInitialized(book.getAuthors())) {
            dto.setAuthorNames(book.getAuthors().stream()
                    .map(User::getName)
                    .collect(Collectors.toList()));
        } else {
            dto.setAuthorNames(Collections.emptyList());
        }

        return dto;
    }
}