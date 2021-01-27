package com.radek.bookstore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    @NotNull(message = "Content cannot be null")
    @Size(min=3, max = 255, message = "Content cannot be shorter than 3 characters and longer than 255 characters")
    private String content;
}
