package com.radek.bookstore.service.impl;

import com.radek.bookstore.model.Category;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.repository.CategoryRepository;
import com.radek.bookstore.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
