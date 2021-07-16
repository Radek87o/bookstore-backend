package com.radek.bookstore.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CommentJson {
    private String id;
    private String content;
    private LocalDateTime updateDate;
    private String usernameToDisplay;
}
