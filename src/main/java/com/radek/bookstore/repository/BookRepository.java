package com.radek.bookstore.repository;

import com.radek.bookstore.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.RequestParam;

import javax.transaction.Transactional;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, String> {
    List<Book> findByTitle(String title);

    @Transactional
    @Query(nativeQuery = true, value = "SELECT * FROM book b JOIN author a ON b.author_id=a.id " +
                                       "WHERE LOWER(b.title) LIKE CONCAT ('%',LOWER(:keyword),'%') " +
                                        "OR LOWER(b.subtitle) LIKE CONCAT ('%',LOWER(:keyword),'%') " +
                                        "OR LOWER(a.first_name) LIKE LOWER(CONCAT ('%',:keyword,'%')) " +
                                        "OR LOWER(a.last_name) LIKE LOWER(CONCAT ('%',:keyword,'%')) " +
                                        "ORDER BY b.created_date DESC")
    Page<Book> findBookByKeyword(@RequestParam("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT b FROM Book b WHERE b.promoPrice!=null AND b.promoPrice>0")
    Page<Book> findBooksWithPromo(Pageable pageable);

    @Query(value = "SELECT b FROM Book b WHERE b.active=TRUE")
    Page<Book> findActiveBooks(Pageable pageable);
}
