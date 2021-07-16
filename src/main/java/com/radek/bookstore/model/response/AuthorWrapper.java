package com.radek.bookstore.model.response;

import com.radek.bookstore.model.Book;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
public class AuthorWrapper {
    private String id;
    private String firstName;
    private String lastName;
    private Page<Book> books;
}
