package com.radek.bookstore.service;

import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.generators.CommentGenerator;
import com.radek.bookstore.generators.UserGenerator;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Comment;
import com.radek.bookstore.model.User;
import com.radek.bookstore.model.dto.CommentDto;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.json.CommentJson;
import com.radek.bookstore.model.mapper.CommentJsonMapper;
import com.radek.bookstore.repository.BookRepository;
import com.radek.bookstore.repository.CommentRepository;
import com.radek.bookstore.repository.UserRepository;
import com.radek.bookstore.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    CommentRepository commentRepository;

    @Mock
    BookRepository bookRepository;

    @Mock
    UserRepository userRepository;

    CommentJsonMapper commentJsonMapper = new CommentJsonMapper();

    CommentService commentService;

    @BeforeEach
    void setup() {
        commentService = new CommentServiceImpl(commentRepository, bookRepository, userRepository, commentJsonMapper);
    }

    @Test
    void shouldGetCommentsByBookIdMethodReturnPageOfComments() {
        String bookId = "testBookId";
        List<Comment> comments = generateExampleOfCommentsPage();
        when(commentRepository.findByBookId(bookId)).thenReturn(comments);

        Page<CommentJson> result = commentService.getCommentsByBookId(bookId, 0);
        CommentJson resultFirstElement = result.getContent().get(0);
        Comment commentsFirstElement = comments.get(0);

        assertEquals(comments.size(), result.getTotalElements());
        assertEquals(commentsFirstElement.getContent(), resultFirstElement.getContent());
        assertEquals(commentsFirstElement.getId(), resultFirstElement.getId());

        verify(commentRepository).findByBookId(bookId);
    }

    @Test
    void shouldGetCommentsByBookIdMethodReturnEmptyPageWhenBookHasNoComments() {
        String bookId = "testBookId";
        when(commentRepository.findByBookId(bookId)).thenReturn(Collections.emptyList());

        Page<CommentJson> result = commentService.getCommentsByBookId(bookId, 0);

        assertEquals(Page.empty(), result);

        verify(commentRepository).findByBookId(bookId);
    }

    @ParameterizedTest
    @MethodSource("setOfCommentsPagesWithUsernames")
    void shouldGetCommentsByBookIdMethodReturnCorrectlyProcessedUsernames(Comment comment, String username) {
        String bookId = "testBookId";
        PageRequest pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "updateDate"));
        List<Comment> comments = Arrays.asList(comment);
        when(commentRepository.findByBookId(bookId)).thenReturn(comments);

        Page<CommentJson> result = commentService.getCommentsByBookId(bookId, 0);

        assertEquals(username, result.getContent().get(0).getUsernameToDisplay());

        verify(commentRepository).findByBookId(bookId);
    }

    private static Stream<Arguments> setOfCommentsPagesWithUsernames() {
        Comment comment1 = CommentGenerator
                .generateCommentWithUsernames("commentId1", "Konstanty", "Walenda");
        Comment comment2 = CommentGenerator
                .generateCommentWithUsernames("commentId2", "K.", "W.");
        Comment comment3 = CommentGenerator
                .generateCommentWithUsernames("commentId2", "", "");
        Comment comment4 = CommentGenerator
                .generateCommentWithUsernames("commentId2", "Ko", "Wal");
        return Stream.of(
                Arguments.of(comment1, "Konstanty Walenda"),
                Arguments.of(comment2, "K. W."),
                Arguments.of(comment3, " "),
                Arguments.of(comment4, "Ko Wal")
        );
    }

    @Test
    void shouldGetCommentsByBookIdMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccur() {
        String bookId = "testBookId";
        doThrow(new NonTransientDataAccessException(""){}).when(commentRepository).findByBookId(bookId);

        assertThrows(BookStoreServiceException.class, () -> commentService.getCommentsByBookId(bookId, 0));
        verify(commentRepository).findByBookId(bookId);
    }

    @Test
    void shouldSaveCommentMethodReturnCollectionOfBooksComments() {
        Comment comment = CommentGenerator.generateCommentWithCommentId("testCommentId");
        Book book = BookGenerator.generateBookWithId(LocalDateTime.now(), "testBookId");
        User user = comment.getUser();
        CommentDto commentDto = CommentGenerator.generateCommentDto();
        Comment commentToSave = new Comment(commentDto);
        commentToSave.setUser(user);
        book.addComment(commentToSave);

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        lenient().when(bookRepository.save(any(Book.class))).thenReturn(book);


        Collection<Comment> result = commentService.saveComment(commentDto, book.getId(), user.getId());

        assertEquals(book.getComments(), result);

        verify(bookRepository).findById(book.getId());
        verify(userRepository).findById(user.getId());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void shouldSaveCommentMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccur() {
        CommentDto commentDto = CommentGenerator.generateCommentDto();
        String bookId = "testBookId";
        String userId = "testUserId";
        Comment comment = new Comment(commentDto);

        Book book = BookGenerator.generateBookWithId(LocalDateTime.now(), "testBookId");
        User user = UserGenerator.generateUser("testUserId");

        book.addComment(comment);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        doThrow(new NonTransientDataAccessException(""){}).when(bookRepository).save(book);

        assertThrows(BookStoreServiceException.class, () -> commentService.saveComment(commentDto, bookId, userId));

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
    }

    private List<Comment> generateExampleOfCommentsPage() {
        Comment comment1 = CommentGenerator.generateCommentWithCommentId("commentId1");
        Comment comment2 = CommentGenerator.generateCommentWithCommentId("commentId2");
        Comment comment3 = CommentGenerator.generateCommentWithCommentId("commentId3");

        return Arrays.asList(comment3, comment2, comment1);
    }
}
