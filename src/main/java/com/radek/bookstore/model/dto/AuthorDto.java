package com.radek.bookstore.model.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthorDto {
    private final String firstName;
    private final String lastName;
}
