package com.radek.bookstore.service.impl;

import com.radek.bookstore.model.Customer;
import com.radek.bookstore.model.Order;
import com.radek.bookstore.model.OrderItem;
import com.radek.bookstore.model.dto.Purchase;
import com.radek.bookstore.model.response.PurchaseJson;
import com.radek.bookstore.repository.CustomerRepository;
import com.radek.bookstore.service.CheckoutService;
import com.radek.bookstore.utils.UniqueId;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Set;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private final CustomerRepository customerRepository;

    public CheckoutServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public PurchaseJson placeOrder(Purchase purchase) {
        Order order = purchase.getOrder();
        String orderTrackingNumber = UniqueId.nextId();
        Set<OrderItem> orderItems = purchase.getOrderItems();
        order.setOrderTrackingNumber(orderTrackingNumber);
        orderItems.forEach(orderItem -> order.addOrderItem(orderItem));
        order.setShippingAddress(purchase.getShippingAddress());
        order.setBillingAddress(purchase.getBillingAddress());
        Customer customer = purchase.getCustomer();
        customer.addOrder(order);
        customerRepository.save(customer);
        return new PurchaseJson(orderTrackingNumber);
    }
}
