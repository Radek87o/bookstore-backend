package com.radek.bookstore.model;

import com.radek.bookstore.model.dto.UserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GenericGenerator(name = "user_id", strategy = "com.radek.bookstore.model.generator.CustomStringGenerator")
    @GeneratedValue(generator = "user_id")
    private String id;

    @NotNull
    @Size(min = 2)
    private String firstName;

    @NotNull
    @Size(min = 2)
    private String lastName;

    @NotNull
    @Email(message = "Given email is not valid")
    private String email;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime lastUpdateDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Comment> comments=new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Rating> ratings=new HashSet<>();

    public User(UserDto userDto) {
        this.firstName=userDto.getFirstName();
        this.lastName=userDto.getLastName();
        this.email=userDto.getEmail();
        this.address=userDto.getAddress();
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
