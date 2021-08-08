package com.radek.bookstore.service.impl;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Customer;
import com.radek.bookstore.model.Order;
import com.radek.bookstore.model.OrderItem;
import com.radek.bookstore.model.dto.Purchase;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.response.PurchaseJson;
import com.radek.bookstore.repository.BookRepository;
import com.radek.bookstore.repository.CustomerRepository;
import com.radek.bookstore.service.CheckoutService;
import com.radek.bookstore.service.EmailService;
import com.radek.bookstore.utils.UniqueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final BookRepository bookRepository;
    private final EmailService emailService;

    public CheckoutServiceImpl(CustomerRepository customerRepository, BookRepository bookRepository, EmailService emailService) {
        this.customerRepository = customerRepository;
        this.bookRepository = bookRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public PurchaseJson placeOrder(Purchase purchase) {
        try {
            Order order = purchase.getOrder();
            String orderTrackingNumber = UniqueId.nextId();
            Set<OrderItem> orderItems = purchase.getOrderItems();
            order.setOrderTrackingNumber(orderTrackingNumber);
            orderItems.forEach(order::addOrderItem);
            orderItems.forEach(orderItem -> decrementUnitsInStock(orderItem.getBookId(), orderItem.getQuantity()));
            order.setShippingAddress(purchase.getShippingAddress());
            order.setBillingAddress(purchase.getBillingAddress());
            order.setCreditCard(purchase.getCreditCard());
            Customer customer = purchase.getCustomer();
            customer.addOrder(order);
            customerRepository.save(customer);
            sendOrderConfirmationToCustomer(customer, order);
            return new PurchaseJson(orderTrackingNumber);
        } catch (NonTransientDataAccessException exc) {
            String message = "Error by attempt to place order";
            log.info(message);
            throw new BookStoreServiceException(message);
        }
    }

    private void sendOrderConfirmationToCustomer(Customer customer, Order order) {
        try {
            emailService.orderSummaryMessage(customer.getEmail(), order, customer.getFirstName());
        } catch (MessagingException exc) {
            String message = format("Error by attempt to send order confirmation to mail: %s", customer.getEmail());
            log.info(message);
        }
    }

    private void decrementUnitsInStock(String bookId, Integer quantity) {
        try {
            if(!bookRepository.existsById(bookId)) {
                String message = format("Cannot find book with id: {}", bookId);
                log.info(message);
                throw new BookStoreServiceException(message);
            }
            Optional<Book> bookOptional = bookRepository.findById(bookId);
            Book book = bookOptional.orElseGet(() -> bookOptional.get());
            if(book.getUnitsInStock()<quantity) {
                String message = format("Cannot process purchase for book with id: {}  due to insufficient number of units in stock. Requested quantity: {}, units in stock: {}", bookId, quantity, book.getUnitsInStock());
                log.info(message);
                throw new BookStoreServiceException(message);
            }
            book.setUnitsInStock(book.getUnitsInStock()-quantity);
            bookRepository.save(book);
        } catch (NonTransientDataAccessException exc) {
            String message = format("Error by attempt to update units in stocks for %s", bookId);
            log.info(message);
            throw new BookStoreServiceException(message);
        }
    }
}
