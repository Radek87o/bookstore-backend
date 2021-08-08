package com.radek.bookstore.model.dto;

import com.radek.bookstore.model.*;
import lombok.Data;

import javax.validation.Valid;
import java.util.Set;

@Data
public class Purchase {

    @Valid
    private Customer customer;

    @Valid
    private Address shippingAddress;

    @Valid
    private Address billingAddress;

    @Valid
    private CreditCard creditCard;

    @Valid
    private Order order;

    @Valid
    private Set<OrderItem> orderItems;
}
