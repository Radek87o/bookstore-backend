package com.radek.bookstore.service;

import com.radek.bookstore.model.Order;
import com.radek.bookstore.model.OrderItem;
import com.radek.bookstore.model.dto.Purchase;
import com.radek.bookstore.model.response.PurchaseJson;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CheckoutService {

    PurchaseJson placeOrder(Purchase purchase);
    Page<Order> findUserOrders(String email, int page, int size);
    List<OrderItem> findOrderItems(String orderId);
}
