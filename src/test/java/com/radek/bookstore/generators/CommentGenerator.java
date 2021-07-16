package com.radek.bookstore.generators;

import com.radek.bookstore.model.Comment;
import com.radek.bookstore.model.User;
import com.radek.bookstore.model.dto.CommentDto;
import com.radek.bookstore.model.response.CommentJson;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CommentGenerator {

    private static final String CONTENT_REGEX_PATTERN = "[A-Za-ząćęłńóśźżĄĘŁŃÓŚŹŻ]{30,}";

    public static Comment generateCommentWithCommentId(String commentId) {
        return generateBaseComment(commentId);
    }

    public static CommentDto generateCommentDto() {
        return new CommentDto(RegexWordGenerator.getRandomRegexWord(CONTENT_REGEX_PATTERN));
    }

    public static CommentJson generateCommentJson(String commentId) {
        return generateCommentBaseJson(commentId);
    }

    public static Comment generateCommentWithUsernames(String commentId, String firstName, String lastName) {
        Comment comment = generateBaseComment(commentId);
        User user = UserGenerator.generateUser("userId_" + firstName + "_" + lastName);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        comment.setUser(user);
        return comment;
    }

    private static CommentJson generateCommentBaseJson(String commentId) {
        CommentJson commentJson = new CommentJson();
        commentJson.setUsernameToDisplay("Jan Nowak");
        commentJson.setContent(RegexWordGenerator.getRandomRegexWord(CONTENT_REGEX_PATTERN));
        commentJson.setId(commentId);
        commentJson.setUpdateDate(LocalDateTime.now());
        return commentJson;
    }

    private static Comment generateBaseComment(String commentId) {
        Comment comment = new Comment();
        comment.setUser(UserGenerator.generateUser("testUserId"));
        LocalDateTime bookCreatedDate = LocalDateTime.of(LocalDate.of(2021, 1, 20), LocalTime.now());
        comment.setBook(BookGenerator.generateBookWithId(bookCreatedDate, "testBookId"));
        comment.setContent(RegexWordGenerator.getRandomRegexWord(CONTENT_REGEX_PATTERN));
        comment.setCreatedDate(LocalDateTime.now());
        comment.setUpdateDate(LocalDateTime.now());
        comment.setId(commentId);
        return comment;
    }
}
