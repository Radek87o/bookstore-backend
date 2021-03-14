package com.radek.bookstore.controller;

import com.radek.bookstore.model.json.CategoryWrapper;
import com.radek.bookstore.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@CrossOrigin
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllCategories() {
        return ResponseHelper.createOkResponse(categoryService.getAllCategories());
    }

    @GetMapping(path = "/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCategoryById(@PathVariable("categoryId") String categoryId,
                                             @RequestParam(name = "page", required = false) Integer page,
                                             @RequestParam(name = "size", required = false) Integer size) {
        if(Objects.isNull(page)) {
            page=0;
        }
        if(Objects.isNull(size)) {
            size=24;
        }
        if(!categoryService.existByCategoryId(categoryId)) {
            String message = String.format("Cannot find category with id: {}", categoryId);
            log.info(message);
            return ResponseHelper.createNotFoundResponse(message);
        }
        CategoryWrapper category = categoryService.findByCategoryId(categoryId, page, size);
        return ResponseHelper.createOkResponse(category);
    }
}
