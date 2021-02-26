package com.radek.bookstore.model.dto;

import com.radek.bookstore.model.Address;
import com.radek.bookstore.model.Customer;
import com.radek.bookstore.model.Order;
import com.radek.bookstore.model.OrderItem;
import lombok.Data;

import java.util.Set;

@Data
public class Purchase {

    private Customer customer;
    private Address shippingAddress;
    private Address billingAddress;
    private Order order;
    private Set<OrderItem> orderItems;
}
