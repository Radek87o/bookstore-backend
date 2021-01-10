package com.radek.bookstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.radek.bookstore.model.dto.AuthorDto;
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
public class Author {

    @Id
    @GenericGenerator(name = "author_id", strategy = "com.radek.bookstore.model.generator.CustomStringGenerator")
    @GeneratedValue(generator = "author_id")
    private String id;
    private String firstName;
    private String lastName;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "author")
    private Set<Book> books;

    public Author(AuthorDto authorDto){
        this.firstName=authorDto.getFirstName();
        this.lastName=authorDto.getLastName();
    }

    public void addBook(Book book) {
        if(Objects.isNull(books)) {
            books=new HashSet<>();
        }
        books.add(book);
        book.setAuthor(this);
    }
}
