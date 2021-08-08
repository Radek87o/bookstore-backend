package com.radek.bookstore.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

import static com.radek.bookstore.utils.CustomRegexPatterns.EMAIL_REGEX;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GenericGenerator(name = "customer_id", strategy = "com.radek.bookstore.model.generator.CustomStringGenerator")
    @GeneratedValue(generator = "customer_id")
    private String id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    @Pattern(regexp = EMAIL_REGEX, message = "Given email is not valid")
    private String email;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer")
    private Set<Order> orders = new HashSet<>();

    public void addOrder(Order order) {
        if(orders==null) {
            orders = new HashSet<>();
        }
        orders.add(order);
        order.setCustomer(this);
    }
}
