package com.radek.bookstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.radek.bookstore.model.dto.CommentDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment {

    @Id
    @GenericGenerator(name = "comment_id", strategy = "com.radek.bookstore.model.generator.CustomStringGenerator")
    @GeneratedValue(generator = "comment_id")
    private String id;
    private String content;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Comment(CommentDto commentDto){
        this.content=commentDto.getContent();
    }
}
