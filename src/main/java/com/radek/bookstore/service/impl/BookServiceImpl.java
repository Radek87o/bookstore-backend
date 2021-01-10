package com.radek.bookstore.service.impl;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.json.BookJson;
import com.radek.bookstore.model.mapper.BookJsonMapper;
import com.radek.bookstore.repository.BookRepository;
import com.radek.bookstore.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookJsonMapper bookJsonMapper;

    public BookServiceImpl(BookRepository bookRepository, BookJsonMapper bookJsonMapper) {
        this.bookRepository = bookRepository;
        this.bookJsonMapper = bookJsonMapper;
    }

    @Override
    public Page<Book> listAllBooks(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
        return bookRepository.findAll(pageable);
    }

    @Override
    public BookJson findBook(String id) {
        Optional<Book> bookOptional = bookRepository.findById(id);
        if(bookOptional.isEmpty()) {
            log.info("Requested book with id: {} is not found", id);
            return null;
        }
        Book book = bookOptional.get();
        BookJson bookJson = bookJsonMapper.map(book, BookJson.class);
        bookJson.setDescription(extractDescriptionParagraphs(book.getDescription()));
        return bookJson;
    }

    @Override
    public boolean existsByBookId(String id) {
        return bookRepository.existsById(id);
    }

    private List<String> extractDescriptionParagraphs(String description) {
        List<String> descriptionParagraphs = new ArrayList<>();
        String[] paragraphs = Objects.nonNull(description) ? description.split("\n") : new String[0];
        for (String paragraph: paragraphs) {
            if(paragraph.trim().length()>0) {
                descriptionParagraphs.add(paragraph);
            }
        }
        return descriptionParagraphs;
    }
}
