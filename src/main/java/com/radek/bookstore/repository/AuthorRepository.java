package com.radek.bookstore.repository;

import com.radek.bookstore.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, String> {

    Optional<Author> findByFirstNameAndLastNameIgnoreCase(String firstName, String lastName);
}
