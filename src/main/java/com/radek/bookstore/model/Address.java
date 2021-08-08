package com.radek.bookstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.radek.bookstore.model.dto.AddressDto;
import com.radek.bookstore.utils.CustomRegexPatterns;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.radek.bookstore.utils.CustomRegexPatterns.ZIP_CODE_REGEX;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Address {

    @Id
    @GenericGenerator(name = "address_id", strategy = "com.radek.bookstore.model.generator.CustomStringGenerator")
    @GeneratedValue(generator = "address_id")
    private String id;

    @Column(name = "city")
    @NotBlank
    private String city;

    @NotBlank
    private String street;

    @NotBlank
    private String locationNumber;

    @NotNull
    @Pattern(regexp = ZIP_CODE_REGEX)
    private String zipCode;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "address")
    private Set<User> users;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Order order;

    public Address(AddressDto addressDto) {
        this.street=addressDto.getStreet();
        this.locationNumber=addressDto.getLocationNumber();
        this.city=addressDto.getCity();
        this.zipCode=addressDto.getZipCode();
    }

    public void addUser(User user) {
        if(Objects.isNull(users)) {
            this.users=new HashSet<>();
        }
        this.users.add(user);
        user.setAddress(this);
    }
}
