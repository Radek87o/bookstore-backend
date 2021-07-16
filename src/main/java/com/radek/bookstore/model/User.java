package com.radek.bookstore.model;

import com.radek.bookstore.model.dto.UserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "email")})
public class User implements Serializable {

    @Id
    @GenericGenerator(name = "user_id", strategy = "com.radek.bookstore.model.generator.CustomStringGenerator")
    @GeneratedValue(generator = "user_id")
    @Column(nullable = false, updatable = false)
    private String id;

    @NotBlank
    private String userId;

    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;

    private String username;

    @NotBlank
    @Email(message = "Given email is not valid")
    private String email;

    @NotBlank
    private String password;

    private String profileImageUrl;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime lastUpdateDate;

    private LocalDateTime lastLoginDate;

    @NotBlank
    private String role;

    private String[] authorities;

    private boolean isActive;

    private boolean isNotLocked;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Comment> comments=new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Rating> ratings=new HashSet<>();

    public User(UserDto userDto) {
        this.firstName=userDto.getFirstName();
        this.lastName=userDto.getLastName();
        this.email=userDto.getEmail();
        this.username=userDto.getUsername();
        this.password=userDto.getPassword();
    }

    public void addComment(Comment comment) {
        if(Objects.isNull(comments)) {
            comments=new HashSet<>();
        }
        comments.add(comment);
        comment.setUser(this);
    }

    public void addRating(Rating rating) {
        if(Objects.isNull(ratings)) {
            ratings=new HashSet<>();
        }
        ratings.add(rating);
        rating.setUser(this);
    }
}
