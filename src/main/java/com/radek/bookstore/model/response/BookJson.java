package com.radek.bookstore.model.response;

import com.radek.bookstore.model.Author;
import com.radek.bookstore.model.Category;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class BookJson {
    private String id;
    private String title;
    private String subtitle;
    private List<String> description;
    private String imageUrl;
    private Integer issueYear;
    private Integer pages;
    private boolean isHardcover;
    private Author author;
    private BigDecimal basePrice;
    private BigDecimal promoPrice;
    private boolean active;
    private Integer unitsInStock;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdateDate;
    private Set<Category> categories;
}
