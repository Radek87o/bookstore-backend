package com.radek.bookstore.service.impl;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Comment;
import com.radek.bookstore.model.User;
import com.radek.bookstore.model.dto.CommentDto;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.response.CommentJson;
import com.radek.bookstore.model.mapper.CommentJsonMapper;
import com.radek.bookstore.repository.BookRepository;
import com.radek.bookstore.repository.CommentRepository;
import com.radek.bookstore.repository.UserRepository;
import com.radek.bookstore.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class CommentServiceImpl implements CommentService {

    private final static Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CommentJsonMapper commentJsonMapper;

    public CommentServiceImpl(CommentRepository commentRepository, BookRepository bookRepository, UserRepository userRepository, CommentJsonMapper commentJsonMapper) {
        this.commentRepository = commentRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.commentJsonMapper = commentJsonMapper;
    }


    @Override
    public Page<CommentJson> getCommentsByBookId(String bookId, Integer pageNumber) {
        try {
            List<Comment> comments = commentRepository.findByBookId(bookId);
            if(comments.isEmpty()) {
                return Page.empty();
            }
            List<CommentJson> commentJsonCollection = mapCommentsToCommentsJson(comments);
            List<CommentJson> sortedCommentJsonCollection = commentJsonCollection.stream()
                    .sorted(Comparator.comparing(CommentJson::getUpdateDate).reversed())
                    .collect(Collectors.toList());
            PagedListHolder<CommentJson> listHolder = new PagedListHolder<>(sortedCommentJsonCollection);
            listHolder.setPage(pageNumber);
            listHolder.setPageSize(10);
            return new PageImpl<>(listHolder.getPageList(),
                                  PageRequest.of(pageNumber, 10),
                                  sortedCommentJsonCollection.size());
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during retrieving comments by bookId";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    @Transactional
    public Collection<Comment> saveComment(CommentDto commentDto, String bookId, String userId) {
        try {
            Book book = bookRepository.findById(bookId).get();
            User user = userRepository.findById(userId).get();
            Comment comment = new Comment(commentDto);
            comment.setUser(user);
            book.addComment(comment);
            Book savedBook = bookRepository.save(book);
            log.info("Succesfully added new comment for book with id: {} by user with id: {}", bookId, userId);
            return savedBook.getComments();
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during attempt to save comment to database";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    private List<CommentJson> mapCommentsToCommentsJson(List<Comment> comments) {
        return comments
                .stream()
                .map(comment -> commentJsonMapper.map(comment, CommentJson.class))
                .map(commentJson -> determineUsernamesToDisplay(comments, commentJson))
                .collect(Collectors.toList());
    }

    private CommentJson determineUsernamesToDisplay(List<Comment> comments, CommentJson commentJson) {
        Comment commentToProcess =  findCommentById(commentJson.getId(), comments);
        String usernameToDisplay = isNotBlank(commentToProcess.getUser().getUsername())
                ? commentToProcess.getUser().getUsername()
                : commentToProcess.getUser().getFirstName()+" "+commentToProcess.getUser().getLastName();
        commentJson.setUsernameToDisplay(usernameToDisplay);
        return commentJson;
    }

    private Comment findCommentById(String commentId, List<Comment> comments) {
        return comments.stream().filter(comment -> comment.getId().equals(commentId)).findFirst().get();
    }


}
