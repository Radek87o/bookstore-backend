package com.radek.bookstore.model;

import com.radek.bookstore.model.dto.*;
import com.radek.bookstore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostConstructRunner {

    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    private final static String DESCRIPTION = "Kolejny kryminał pióra Jane Harper, autorki światowych bestsellerów, takich jak Susza i Siły natury, potwierdza jej umiejętności w kreowaniu zagadek i mrocznej atmosfery. Spalone słońcem pustkowia stają się świadkiem odkrywanych powoli tajemnic, jakie ma każda pozornie idealna rodzina.\n" +
            "\n" +
            "Bracia Bright mieszkają trzy godziny jazdy samochodem od siebie. Na australijskim pustkowiu czyni ich to najbliższymi sąsiadami. Nathan i Bub przez kilka miesięcy się nie widzieli. Gdy się spotykają, Cameron, trzeci z ich rodzeństwa, leży martwy u ich stóp.\n" +
            "\n" +
            "Cameron prowadził rodzinne gospodarstwo. Zostawił żonę, córki oraz matkę. Coś, czego ani Nathan, ani Bub nie są w stanie sobie wyobrazić, sprawiło, że ich brat znalazł się sam na spalonym słońcem pustkowiu.\n" +
            "\n" +
            "Na ranczu zmarłego oprócz rodziny mieszkają jeszcze wieloletni pracownicy oraz dwaj zatrudnieni niedawno sezonowcy. Mimo żałoby Nathan nie może uwolnić się od podejrzeń. Stopniowo odkrywa sekrety, o których jego rodzina wolałaby zapomnieć.\n" +
            "\n" +
            "Jeśli ktoś doprowadził do śmierci Camerona, to nie mógł się ukryć pośrodku niczego.";

//    @PostConstruct
    private void populateCategories(){
        List<Category> categoriesToPersist = Arrays.asList(
                new Category(CategoryDto.builder().name("Fantasy").build())
//                new Category(CategoryDto.builder().name("Literatura piękna").build()),
//                new Category(CategoryDto.builder().name("Dla dzieci i młodzieży").build()),
//                new Category(CategoryDto.builder().name("Fantastyka").build()),
//                new Category(CategoryDto.builder().name("Hobby").build()),
//                new Category(CategoryDto.builder().name("Komiksy").build()),
//                new Category(CategoryDto.builder().name("Kryminały i sensacja").build()),
//                new Category(CategoryDto.builder().name("Dla Kobiet").build()),
//                new Category(CategoryDto.builder().name("Naukowe i popularnonaukowe").build()),
//                new Category(CategoryDto.builder().name("Poradniki").build()),
//                new Category(CategoryDto.builder().name("Kuchnia, diety i przepisy").build()),
//                new Category(CategoryDto.builder().name("Literatura faktu i reportaż").build()),
//                new Category(CategoryDto.builder().name("Nauka języków").build()),
//                new Category(CategoryDto.builder().name("Podróże i turystyka").build())
        );

        categoryRepository.saveAll(categoriesToPersist);
    }

//    @PostConstruct
    private void populateBooks() {
        List<Book> books = Arrays.asList(
                new Book(BookDto.builder()
                        .title("Droga królów")
                        .active(true)
                        .imageUrl("https://cdn-lubimyczytac.pl/upload/books/4896000/4896952/787493-352x500.jpg")
                        .basePrice(BigDecimal.valueOf(41.99))
                        .author(authorRepository.findById("aa550c1a-bfca-4cd3-be9e-bc07736f40c7").get())
                        .unitsInStock(100)
                        .isHardcover(true)
                        .issueYear(2014)
                        .pages(1136)
                        .build()),
                new Book(BookDto.builder()
                        .title("Słowa światłości")
                        .active(true)
                        .imageUrl("https://cdn-lubimyczytac.pl/upload/books/4905000/4905352/787492-352x500.jpg")
                        .basePrice(BigDecimal.valueOf(37.99))
                        .author(authorRepository.findById("aa550c1a-bfca-4cd3-be9e-bc07736f40c7").get())
                        .unitsInStock(100)
                        .isHardcover(true)
                        .issueYear(2014)
                        .pages(1200)
                        .build())
//                new Book(BookDto.builder()
//                        .active(true)
//                        .author(authorRepository.getOne("ae84a436-ff63-4f1c-992f-8057190f65b1"))
//                        .basePrice(BigDecimal.valueOf(25.50))
//                        .title("Światło ukryte w mroku")
//                        .imageUrl("https://cdn-lubimyczytac.pl/upload/books/4909000/4909064/787616-352x500.jpg")
//                        .unitsInStock(100)
//                        .build()),
//                new Book(BookDto.builder()
//                        .active(true)
//                        .author(authorRepository.getOne("fd982793-d4d7-471c-a2f6-456cc043b527"))
//                        .basePrice(BigDecimal.valueOf(23))
//                        .title("Szczyty chciwości")
//                        .imageUrl("https://cdn-lubimyczytac.pl/upload/books/4919000/4919005/797640-352x500.jpg")
//                        .unitsInStock(100)
//                        .build()),
//                new Book(BookDto.builder()
//                        .active(true)
//                        .author(authorRepository.getOne("916bcafb-26c0-4517-8573-ed4e86b5183b"))
//                        .basePrice(BigDecimal.valueOf(30.55))
//                        .title("Stracony")
//                        .imageUrl("https://cdn-lubimyczytac.pl/upload/books/4911000/4911912/783690-352x500.jpg")
//                        .unitsInStock(100)
//                        .build()),
//                new Book(BookDto.builder()
//                        .active(true)
//                        .author(authorRepository.getOne("11fd81e5-65e3-4689-a773-6c555f258d67"))
//                        .basePrice(BigDecimal.valueOf(29.79))
//                        .title("Krzyk zagubionych serc")
//                        .imageUrl("https://cdn-lubimyczytac.pl/upload/books/4885000/4885916/794724-352x500.jpg")
//                        .unitsInStock(100)
//                        .build()),
//                new Book(BookDto.builder()
//                        .active(true)
//                        .author(authorRepository.getOne("fd982793-d4d7-471c-a2f6-456cc043b527"))
//                        .basePrice(BigDecimal.valueOf(22.50))
//                        .title("Cień zbrodni")
//                        .imageUrl("https://cdn-lubimyczytac.pl/upload/books/4910000/4910430/780595-352x500.jpg")
//                        .unitsInStock(100)
//                        .build())
        );

        bookRepository.saveAll(books);
    }

//    @PostConstruct
    private void addCategories(){
        Book book1 = bookRepository.findById("6c39e3e2-29a0-4773-84ec-25bceba82e2b").get();
        Book book2 = bookRepository.findById("577d78c3-52be-4651-8181-485af1162515").get();

        Category category = categoryRepository.findById("f12b80c8-071f-4d04-b10d-6f0a285c6df1").get();

        book1.addCategory(category);
        book2.addCategory(category);

        categoryRepository.save(category);
        bookRepository.save(book1);
        bookRepository.save(book2);
    }

//    @PostConstruct
    private void createUsers() {
        List<User> users = Arrays.asList(
                new User(UserDto.builder()
                        .firstName("Andrzej")
                        .lastName("Nowak")
                        .email("andrzej.nowak@gmail.com")
                        .build()),
                new User(UserDto.builder()
                        .firstName("Krzysztof")
                        .lastName("Banaszek")
                        .email("krzysztof.banaszek@gmail.com")
                        .build()),
                new User(UserDto.builder()
                        .firstName("Konstanty")
                        .lastName("Walenda")
                        .email("konstanty.walenda@yandex.com")
                        .build())
        );
        userRepository.saveAll(users);
    }

//    @PostConstruct
    private void addCommentByUserToBook() {
        Comment comment = new Comment(new CommentDto("Wciągająca od pierwszej do ostatniej strona - mimo że ostatnia jest strona z okładką"));
        Book book = bookRepository.findById("6c39e3e2-29a0-4773-84ec-25bceba82e2b").get();
        User user = userRepository.findById("0c860ac0-34d7-4538-8a7f-3b2dc7444c6d").get();
        comment.setUser(user);
        book.addComment(comment);
        bookRepository.save(book);
    }

//    @PostConstruct
    private void addRatingByUserToBook() {
        Rating rating = new Rating(new RatingDto(4));
        Book book = bookRepository.findById("6c39e3e2-29a0-4773-84ec-25bceba82e2b").get();
        User user = userRepository.findById("408a2c8b-ea95-4c81-b952-2e4a124a1b50").get();
//        rating.setBook(book);
//        rating.setUser(user);
//        Rating savedRating = ratingRepository.save(rating);
        rating.setUser(user);
        book.addRating(rating);
        bookRepository.save(book);
    }

//    @PostConstruct
    private void deleteRedundantRatings() {
        ratingRepository.deleteById("8c93a51a-3a8c-4287-86f5-ee7461212c5e");
        ratingRepository.deleteById("a1bde8b1-2a5b-497e-bfe9-363934235302");
        ratingRepository.deleteById("e1058e42-2a63-4ef9-870a-4e6129b45f7a");
    }




//    @PostConstruct
    private void populateAuthors() {
        List<Author> authors = Arrays.asList(
                new Author(AuthorDto.builder().firstName("Brandon").lastName("Sanderson").build())
//                new Author(AuthorDto.builder().firstName("Sharon").lastName("Cameron").build()),
//                new Author(AuthorDto.builder().firstName("Edyta").lastName("Świętek").build()),
//                new Author(AuthorDto.builder().firstName("Jane").lastName("Harper").build()),
//                new Author(AuthorDto.builder().firstName("Joanna").lastName("Jax").build())
        );

        authorRepository.saveAll(authors);
    }

//    @PostConstruct
    private void populateDescription() {
        Optional<Book> book = bookRepository.findById("bfece0b8-de82-48cc-af88-2fb47c5aa7f1");
        if(book.isPresent()) {
            Book bookToPersist = book.get();
            List<String> descriptionParagraphs = new ArrayList<>();
            String[] paragraphs = bookToPersist.getDescription().split("\n");
            for (String paragraph: paragraphs) {
                if(paragraph.trim().length()>0) {
                    descriptionParagraphs.add(paragraph);
                }
            }
            System.out.println("Jest super!");
//            bookToPersist.setDescription(DESCRIPTION);
//            bookRepository.save(bookToPersist);
        }
    }
}
