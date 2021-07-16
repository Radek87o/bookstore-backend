package com.radek.bookstore.service;

import com.radek.bookstore.model.Category;
import com.radek.bookstore.model.response.CategoryWrapper;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();
    CategoryWrapper findByCategoryId(String categoryId, Integer page, Integer size);
    Boolean existByCategoryId(String categoryId);
}
