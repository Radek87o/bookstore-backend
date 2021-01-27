package com.radek.bookstore.model.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder
@Getter
public class AuthorDto {

    @NotNull(message = "First name cannot be null")
    @Size(min=2, max = 30, message = "First name cannot be shorter than 2 characters and longer than 30 characters")
    private final String firstName;

    @NotNull(message = "Last name cannot be null")
    @Size(min=2, max = 30, message = "Last name cannot be shorter than 2 characters and longer than 30 characters")
    private final String lastName;
}
