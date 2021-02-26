package com.radek.bookstore.model;

import com.radek.bookstore.model.dto.AddressDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Address {

    @Id
    @GenericGenerator(name = "address_id", strategy = "com.radek.bookstore.model.generator.CustomStringGenerator")
    @GeneratedValue(generator = "address_id")
    private String id;

    @NotNull
    private String street;

    @NotNull
    private String locationNumber;

    @NotNull
    private String city;

    @NotNull
    @Pattern(regexp = "^(\\d{2}-\\d{3})$")
    private String zipCode;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Order order;

    public Address(AddressDto addressDto) {
        this.street=addressDto.getStreet();
        this.locationNumber=addressDto.getLocationNumber();
        this.city=addressDto.getCity();
        this.zipCode=addressDto.getZipCode();
    }
}
