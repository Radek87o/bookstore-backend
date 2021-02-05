package com.radek.bookstore.service;

import com.radek.bookstore.model.Category;
import com.radek.bookstore.model.json.CategoryWrapper;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    List<Category> getAllCategories();
    CategoryWrapper findByCategoryId(String categoryId, Integer page, Integer size);
    Boolean existByCategoryId(String categoryId);
}
