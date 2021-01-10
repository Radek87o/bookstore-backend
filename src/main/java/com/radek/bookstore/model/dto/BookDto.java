package com.radek.bookstore.model.dto;

import com.radek.bookstore.model.Author;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BookDto {

    private final String title;
    private final String subtitle;
    private final String description;
    private final String imageUrl;
    private final Integer issueYear;
    private final Integer pages;
    private final boolean isHardcover;
    private final Author author;
    private final BigDecimal basePrice;
    private final BigDecimal promoPrice;
    private final boolean active;
    private final Integer unitsInStock;
}
