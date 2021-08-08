package com.radek.bookstore.generators;

import com.radek.bookstore.model.Order;

import java.math.BigDecimal;

import static com.radek.bookstore.generators.AddressGenerator.generateBaseAddress;
import static com.radek.bookstore.generators.CreditCardGenerator.generateCreditCard;
import static com.radek.bookstore.generators.CustomerGenerator.generateCustomer;

public class OrderGenerator {

    public static Order generateOrder() {
        return generateBaseOrder();
    }

    public static Order generateOrderWithTotalQuantity(Integer quantity) {
        Order order = generateBaseOrder();
        order.setTotalQuantity(quantity);
        return order;
    }

    public static Order generateOrderWithTotalPrice(BigDecimal totalPrice) {
        Order order = generateBaseOrder();
        order.setTotalPrice(totalPrice);
        return order;
    }

    private static Order generateBaseOrder() {
        Order order = new Order();
        order.setId("someOrderId");
        order.setTotalPrice(BigDecimal.valueOf(76.5));
        order.setOrderTrackingNumber("someOrderTrackingId");
        order.setTotalQuantity(1);
        order.setCustomer(generateCustomer());
        order.setBillingAddress(generateBaseAddress());
        order.setShippingAddress(generateBaseAddress());
        order.setCreditCard(generateCreditCard());
        return order;
    }
}
