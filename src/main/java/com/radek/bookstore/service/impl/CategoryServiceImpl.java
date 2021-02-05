package com.radek.bookstore.service.impl;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Category;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.json.CategoryWrapper;
import com.radek.bookstore.repository.CategoryRepository;
import com.radek.bookstore.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final static Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        try {
            return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        } catch(NonTransientDataAccessException exc) {
            String message = "An error occurred during retrieving categories from db";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public CategoryWrapper findByCategoryId(String categoryId, Integer page, Integer size) {
        try {
            Category category = categoryRepository.findById(categoryId).get();
            Set<Book> categoriesBooks = Objects.isNull(category.getBooks()) ? new HashSet<>() : category.getBooks();
            List<Book> booksList = categoriesBooks.stream()
                    .sorted(Comparator.comparing(Book::getCreatedDate).reversed())
                    .collect(Collectors.toList());
            PageImpl<Book> books = new PageImpl<>(booksList, PageRequest.of(page, size), booksList.size());
            return CategoryWrapper.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .books(books)
                    .build();
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during retrieving category by id";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public Boolean existByCategoryId(String categoryId) {
        try {
            return categoryRepository.existsById(categoryId);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during checking if category with given id exists";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }
}
