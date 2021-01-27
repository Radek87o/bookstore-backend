package com.radek.bookstore.model.dto;

import com.radek.bookstore.model.Category;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@Builder
public class BookDto {

    @NotNull(message = "Title cannot be null")
    @Size(min=2, message = "Title cannot be shorter than 2 characters")
    private final String title;

    @Size(min=2, message = "Subtitle cannot be shorter than 2 characters")
    private final String subtitle;

    @NotNull(message = "Description cannot be null")
    @Size(min = 200, max = 2000, message = "Description cannot be shorter than 200 characters and longer than 2000 characters")
    private final String description;

    @NotNull(message = "ImageUrl cannot be null")
    private final String imageUrl;

    @NotNull(message = "Issue year cannot be null")
    @Min(value = 2000L, message = "Issue year cannot be earlier than 2000")
    @Max(value = 2022L, message = "Issue year cannot be greater than 2022")
    private final Integer issueYear;

    @Min(value = 1L, message = "Number of pages cannot be smaller than 1")
    @Max(value = 9999L, message = "Number of pages cannot be greater than 9999")
    private final Integer pages;

    @NotNull(message = "Field cannot be null")
    private final boolean isHardcover;

    @NotNull(message = "Author cannot be null")
    @Valid
    private AuthorDto author;

    @NotNull(message = "Base price cannot be null")
    @DecimalMax(value = "999.99", message = "Base price value cannot be greater than 999.99")
    private BigDecimal basePrice;

    @DecimalMax(value = "999.99", message = "Promo price value cannot be greater than 999.99")
    private BigDecimal promoPrice;

    @NotNull(message = "Field cannot be null")
    private final boolean active;

    @Min(value = 0L, message = "Number of units in stock cannot be smaller than 0")
    @Max(value = 9999L, message = "Number of units in stock cannot be greater than 9999")
    private final Integer unitsInStock;

    @NotNull(message = "Categories field cannot be null")
    @NotEmpty(message = "Categories field cannot be empty")
    private Set<Category> categories;
}
