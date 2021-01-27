package com.radek.bookstore.generators;

import com.radek.bookstore.model.Author;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Category;
import com.radek.bookstore.model.dto.AuthorDto;
import com.radek.bookstore.model.dto.BookDto;
import com.radek.bookstore.model.dto.CategoryDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BookGenerator {

    private static final String NAME_REGEX_PATTERN = "[A-Za-ząćęłńóśźżĄĘŁŃÓŚŹŻ]{6,}";
    private static final String IMAGE_URL = "https://cdn-lubimyczytac.pl/upload/books/4942000/4942761/848896-352x500.jpg";

    public static final String FIVE_PARAGRAPH_DESCRIPTION = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
            "\n" +
            "Vivamus pellentesque venenatis ligula et hendrerit. Phasellus eu porttitor purus.\n" +
            "\n" +
            "Suspendisse quis neque sem. Donec finibus ultricies metus quis laoreet.\n" +
            "\n" +
            "Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.\n" +
            "\n" +
            "Curabitur vel eros eget elit tincidunt finibus et et libero.";

    public static final String FOUR_PARAGRAPH_DESCRIPTION = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
            "\n" +
            "Vivamus pellentesque venenatis ligula et hendrerit. Phasellus eu porttitor purus.\n" +
            "\n" +
            "Suspendisse quis neque sem. Donec finibus ultricies metus quis laoreet.\n" +
            "\n" +
            "Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.";

    public static final String THREE_PARAGRAPH_DESCRIPTION = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
            "\n" +
            "Vivamus pellentesque venenatis ligula et hendrerit. Phasellus eu porttitor purus.\n" +
            "\n" +
            "Suspendisse quis neque sem. Donec finibus ultricies metus quis laoreet.";

    public static final String DESCRIPTION = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
            "\n" +
            "Vivamus pellentesque venenatis ligula et hendrerit. Phasellus eu porttitor purus.";


    private static final String title = RegexWordGenerator.getRandomRegexWord(NAME_REGEX_PATTERN);
    private static final Integer issueYear = ThreadLocalRandom.current().nextInt(2000, 2023);
    private static final Integer pages = ThreadLocalRandom.current().nextInt(100, 1000);
    private static final String authorFirstName = RegexWordGenerator.getRandomRegexWord(NAME_REGEX_PATTERN);
    private static final String authorLastName = RegexWordGenerator.getRandomRegexWord(NAME_REGEX_PATTERN);

    public static Book generateBookWithDescription(LocalDateTime createdTimestamp, String bookId, String description) {
        Book book = generateBook(createdTimestamp, bookId);
        book.setDescription(description);
        return book;
    }

    public static Book generateBookWithId(LocalDateTime createdTimestamp, String bookId) {
        return generateBook(createdTimestamp, bookId);
    }

    public static Book generateBook(LocalDateTime createdTimestamp) {
        return generateBook(createdTimestamp, null);
    }

    public static BookDto generateBookDto(String title, String authorFirstName, String authorLastName) {
        BookDto bookDto = generateBookDto(title, issueYear, pages);
        AuthorDto authorDto = generateAuthorDto(authorFirstName, authorLastName);
        bookDto.setAuthor(authorDto);
        return bookDto;
    }

    public static BookDto generateBookDtoWithTitle(String title) {
        return generateStandardBookDtoBuilder().title(title).build();
    }

    public static BookDto generateBookDtoWithSubtitle(String subtitle) {
        return generateStandardBookDtoBuilder().subtitle(subtitle).build();
    }

    public static BookDto generateBookDtoWithDescription(String description) {
        return generateStandardBookDtoBuilder().description(description).build();
    }

    public static BookDto generateBookDtoWithImageUrl(String imageUrl) {
        return generateStandardBookDtoBuilder().imageUrl(imageUrl).build();
    }

    public static BookDto generateBookDtoWithIssueYear(Integer issueYear) {
        return generateStandardBookDtoBuilder().issueYear(issueYear).build();
    }

    public static BookDto generateBookDtoWithPages(Integer pages) {
        return generateStandardBookDtoBuilder().pages(pages).build();
    }

    public static BookDto generateBookDtoWithAuthor(AuthorDto authorDto) {
        return generateStandardBookDtoBuilder().author(authorDto).build();
    }

    public static BookDto generateBookDtoWithBasePrice(BigDecimal basePrice) {
        return generateStandardBookDtoBuilder().basePrice(basePrice).build();
    }

    public static BookDto generateBookDtoWithPromoPrice(BigDecimal promoPrice) {
        return generateStandardBookDtoBuilder().promoPrice(promoPrice).build();
    }

    public static BookDto generateBookDtoWithUnitsInStock(Integer unitsInStock) {
        return generateStandardBookDtoBuilder().unitsInStock(unitsInStock).build();
    }

    public static BookDto generateBookDtoWithCategories(Set<Category> categories) {
        return generateStandardBookDtoBuilder().categories(categories).build();
    }

    public static BookDto generateBookDtoWithBasePriceAndPromoPrice(BigDecimal basePrice, BigDecimal promoPrice) {
        return generateStandardBookDtoBuilder()
                .basePrice(basePrice)
                .promoPrice(promoPrice)
                .build();
    }

    public static BookDto generateBookDto() {
        return generateStandardBookDto();
    }

    private static BookDto generateStandardBookDto() {
        BookDto bookDto = generateBookDto(title, issueYear, pages);
        AuthorDto authorDto = generateAuthorDto(authorFirstName, authorLastName);
        bookDto.setAuthor(authorDto);
        return bookDto;
    }

    private static String generateRandomId() {
        return UUID.randomUUID().toString();
    }

    private static Book generateBook(LocalDateTime createdTimestamp, String id) {
        String bookId = Objects.isNull(id) ? generateRandomId() : id;

        AuthorDto authorDto = generateAuthorDto(authorFirstName, authorLastName);

        BookDto bookDto = generateBookDto(title, issueYear, pages);

        Book book = new Book(bookDto);
        Author author = new Author(authorDto);
        author.setId(generateRandomId());
        book.setAuthor(author);
        book.setId(bookId);
        book.setCreatedDate(createdTimestamp);
        return book;
    }

    private static AuthorDto generateAuthorDto(String firstName, String lastName) {
        return AuthorDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    private static BookDto generateBookDto(String title, Integer issueYear, Integer pages) {
        return generateStandardBookDtoBuilder()
                .title(title)
                .issueYear(issueYear)
                .pages(pages)
                .build();
    }

    private static BookDto.BookDtoBuilder generateStandardBookDtoBuilder() {
        return BookDto.builder()
                .title(title)
                .description(FIVE_PARAGRAPH_DESCRIPTION)
                .imageUrl(IMAGE_URL)
                .issueYear(issueYear)
                .pages(pages)
                .isHardcover(false)
                .basePrice(BigDecimal.valueOf(29.99))
                .promoPrice(BigDecimal.valueOf(23.99))
                .active(true)
                .unitsInStock(100)
                .subtitle("Some subtitle")
                .author(generateAuthorDto("Jan", "Nowak"))
                .categories(Collections.singleton(new Category(new CategoryDto("Dla dzieci i młodzieży"))));
    }
}
