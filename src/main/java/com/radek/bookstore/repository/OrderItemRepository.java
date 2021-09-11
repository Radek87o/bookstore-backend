package com.radek.bookstore.repository;

import com.radek.bookstore.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    @Query(nativeQuery = true, value = "SELECT * FROM order_item WHERE order_id=:orderId")
    List<OrderItem> findByOrderId(@Param("orderId") String orderId);
}
