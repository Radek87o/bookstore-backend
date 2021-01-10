package com.radek.bookstore.service.impl;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Comment;
import com.radek.bookstore.model.User;
import com.radek.bookstore.model.dto.CommentDto;
import com.radek.bookstore.model.json.CommentJson;
import com.radek.bookstore.model.mapper.CommentJsonMapper;
import com.radek.bookstore.repository.BookRepository;
import com.radek.bookstore.repository.CommentRepository;
import com.radek.bookstore.repository.UserRepository;
import com.radek.bookstore.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

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
    public Page<CommentJson> getCommentsByBookId(String bookId, Integer pageNumber, Integer pageSize) {
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "updateDate"));
        Page<Comment> comments = commentRepository.findByBookId(bookId, pageable);
        if(comments.getContent().isEmpty()) {
            return Page.empty();
        }
        List<CommentJson> commentJsonCollection = comments.getContent()
                .stream()
                .map(comment -> commentJsonMapper.map(comment, CommentJson.class))
                .map(commentJson -> determineUsernamesToDisplay(comments.getContent(), commentJson))
                .collect(Collectors.toList());
        return new PageImpl<>(commentJsonCollection);
    }

    @Override
    @Transactional
    public void saveComment(CommentDto commentDto, String bookId, String userId) {
        Book book = bookRepository.findById(bookId).get();
        User user = userRepository.findById(userId).get();
        Comment comment = new Comment(commentDto);
        comment.setUser(user);
        book.addComment(comment);
        bookRepository.save(book);
        log.info("Succesfully added new comment for book with id: {} by user with id: {}", bookId, userId);
    }

    private CommentJson determineUsernamesToDisplay(List<Comment> comments, CommentJson commentJson) {
        Comment commentToProcess =  findCommentById(commentJson.getId(), comments);
        String usernameToDisplay = commentToProcess.getUser().getFirstName()+" "+commentToProcess.getUser().getLastName();
        commentJson.setUsernameToDisplay(usernameToDisplay);
        return commentJson;
    }

    private Comment findCommentById(String commentId, List<Comment> comments) {
        return comments.stream().filter(comment -> comment.getId().equals(commentId)).findFirst().get();
    }


}
