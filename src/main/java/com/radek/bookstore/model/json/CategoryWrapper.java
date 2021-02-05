package com.radek.bookstore.model.json;

import com.radek.bookstore.model.Book;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
public class CategoryWrapper {

    private String id;
    private String name;
    private Page<Book> books;
}
