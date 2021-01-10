package com.radek.bookstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.radek.bookstore.model.dto.BookDto;
import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GenericGenerator(name = "book_id", strategy = "com.radek.bookstore.model.generator.CustomStringGenerator")
    @GeneratedValue(generator = "book_id")
    private String id;

    @NotNull
    private String title;
    private String subtitle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    private String imageUrl;
    private Integer issueYear;
    private Integer pages;
    private boolean isHardcover;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    private BigDecimal basePrice;
    private BigDecimal promoPrice;

    private boolean active;
    private Integer unitsInStock;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime lastUpdateDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "books_categories",
            joinColumns = @JoinColumn(name="book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "book")
    private Set<Comment> comments=new HashSet<>();

    @JsonIgnore
    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "book")
    private Set<Rating> ratings=new HashSet<>();

    public Book(BookDto bookDto){
        this.title=bookDto.getTitle();
        this.subtitle=bookDto.getSubtitle();
        this.description=bookDto.getDescription();
        this.imageUrl=bookDto.getImageUrl();
        this.issueYear=bookDto.getIssueYear();
        this.pages=bookDto.getPages();
        this.isHardcover=bookDto.isHardcover();
        this.basePrice=bookDto.getBasePrice();
        this.author=bookDto.getAuthor();
        this.basePrice=bookDto.getBasePrice();
        this.promoPrice=bookDto.getPromoPrice();
        this.active=bookDto.isActive();
        this.unitsInStock=bookDto.getUnitsInStock();
    }

    public void addComment(Comment comment) {
        if(Objects.isNull(comments)) {
            comments=new HashSet<>();
        }
        comments.add(comment);
        comment.setBook(this);
    }

    public void addRating(Rating rating) {
        if(Objects.isNull(ratings)) {
            ratings=new HashSet<>();
        }
        ratings.add(rating);
        rating.setBook(this);
    }

    public void addCategory(Category category) {
        if(Objects.isNull(this.categories)) {
            this.categories=new HashSet<>();
        }
        this.categories.add(category);
        category.addBook(this);
    }
}
