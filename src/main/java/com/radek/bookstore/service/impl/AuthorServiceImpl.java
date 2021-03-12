package com.radek.bookstore.service.impl;

import com.radek.bookstore.model.Author;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.json.AuthorWrapper;
import com.radek.bookstore.repository.AuthorRepository;
import com.radek.bookstore.service.AuthorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthorServiceImpl implements AuthorService {

    private static final Logger log = LoggerFactory.getLogger(AuthorServiceImpl.class);

    private AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public AuthorWrapper findByAuthorId(String id, Integer page, Integer size) {
        try {
            Author author = authorRepository.findById(id).get();
            Set<Book> authorBooks = Objects.isNull(author.getBooks()) ? new HashSet<>() : author.getBooks();
            List<Book> booksList = authorBooks.stream()
                    .sorted(Comparator.comparing(Book::getLastUpdateDate).reversed())
                    .collect(Collectors.toList());
            PagedListHolder pagedListHolder = new PagedListHolder(booksList);
            pagedListHolder.setPage(page);
            pagedListHolder.setPageSize(size);
            Page<Book> books = new PageImpl<Book>(pagedListHolder.getPageList(), PageRequest.of(page, size), booksList.size());
            return AuthorWrapper.builder()
                    .id(id)
                    .firstName(author.getFirstName())
                    .lastName(author.getLastName())
                    .books(books)
                    .build();
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during retrieving author by id";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public Boolean existByAuthorId(String id) {
        try {
            return authorRepository.existsById(id);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during checking whether author exists";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }
}
