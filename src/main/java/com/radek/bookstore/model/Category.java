package com.radek.bookstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.radek.bookstore.model.dto.CategoryDto;
import com.radek.bookstore.utils.UniqueId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GenericGenerator(name = "category_id", strategy = "com.radek.bookstore.model.generator.CustomStringGenerator")
    @GeneratedValue(generator = "category_id")
    private String id;
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "categories", fetch = FetchType.EAGER)
    private Set<Book> books;

    public Category(CategoryDto categoryDto) {
        this.name=categoryDto.getName();
    }

    public void addBook(Book book) {
        if(Objects.isNull(books)){
            this.books=new HashSet<>();
        }
        this.books.add(book);
    }
}
