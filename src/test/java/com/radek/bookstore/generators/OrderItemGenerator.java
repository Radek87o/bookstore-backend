package com.radek.bookstore.generators;

import com.radek.bookstore.model.OrderItem;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.radek.bookstore.generators.OrderGenerator.generateOrder;

public class OrderItemGenerator {

    private static final String IMAGE_URL = "https://cdn-lubimyczytac.pl/upload/books/4942000/4942761/848896-352x500.jpg";

    public static Set<OrderItem> generateSetOfOrderItems() {
        return Collections.singleton(generateOrderItem("bookId1", "Krzyżacy", BigDecimal.valueOf(25.5)));
    }

    public static OrderItem generateOrderItemWithTitle(String title) {
        return generateOrderItem("someBookId", title, BigDecimal.valueOf(25.5));
    }

    public static OrderItem generateOrderItemWithBookId(String bookId) {
        return generateOrderItem(bookId, "Krzyżacy", BigDecimal.valueOf(25.5));
    }

    public static OrderItem generateOrderItemWithUnitPrice(BigDecimal unitPrice) {
        return generateOrderItem("someBookId", "Krzyżacy", unitPrice);
    }

    public static OrderItem generateOrderItemWithQuantity(Integer quantity) {
        OrderItem orderItem = generateOrderItem("someBookId", "Krzyżacy", BigDecimal.valueOf(25.5));
        orderItem.setQuantity(quantity);
        return orderItem;
    }

    private static OrderItem generateOrderItem(String bookId, String title, BigDecimal unitPrice) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId("someOrderItemId");
        orderItem.setBookId(bookId);
        orderItem.setQuantity(1);
        orderItem.setImageUrl(IMAGE_URL);
        orderItem.setUnitPrice(unitPrice);
        orderItem.setTitle(title);
        return orderItem;
    }
}
