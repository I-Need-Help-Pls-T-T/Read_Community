package com.univer.bookcom.service.mapper;

import com.univer.bookcom.model.Book;
import com.univer.bookcom.model.Comments;
import com.univer.bookcom.model.User;
import com.univer.bookcom.model.dto.request.CommentsRequestDto;
import com.univer.bookcom.model.dto.response.CommentsResponseDto;
import org.springframework.stereotype.Component;

@Component
public class CommentsMapper {

    public Comments toEntity(CommentsRequestDto dto) {
        Comments comment = new Comments();
        comment.setText(dto.getText());

        User user = new User();
        user.setId(dto.getUserId());
        comment.setUser(user);

        Book book = new Book();
        book.setId(dto.getBookId());
        comment.setBook(book);

        return comment;
    }

    public CommentsResponseDto toResponseDto(Comments comment) {
        CommentsResponseDto dto = new CommentsResponseDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setCreatedAt(comment.getCreatedAt());

        if (comment.getUser() != null) {
            dto.setUserName(comment.getUser().getName());
        }

        if (comment.getBook() != null) {
            dto.setBookTitle(comment.getBook().getTitle());
        }

        return dto;
    }
}