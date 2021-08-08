package com.radek.bookstore.service;

import com.radek.bookstore.generators.BookGenerator;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.Customer;
import com.radek.bookstore.model.dto.Purchase;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.response.PurchaseJson;
import com.radek.bookstore.repository.BookRepository;
import com.radek.bookstore.repository.CustomerRepository;
import com.radek.bookstore.service.impl.CheckoutServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.NonTransientDataAccessException;

import javax.mail.MessagingException;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.radek.bookstore.generators.PurchaseGenerator.generatePurchase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckoutServiceTest {

    @Mock
    CustomerRepository customerRepository;

    @Mock
    BookRepository bookRepository;

    @Mock
    EmailService emailService;

    CheckoutService checkoutService;

    @BeforeEach
    void setup() {
        this.checkoutService = new CheckoutServiceImpl(customerRepository, bookRepository, emailService);
    }

    @Test
    void shouldPlaceOrderMethodSaveOrderWhenIsCorrect() throws MessagingException {
        Purchase purchase = generatePurchase();
        Customer savedCustomer = purchase.getCustomer();
        savedCustomer.setId("someCustomerId");
        Book book = BookGenerator.generateBookWithId(LocalDateTime.now(), "bookId");

        when(bookRepository.existsById(anyString())).thenReturn(true);
        when(bookRepository.findById(anyString())).thenReturn(Optional.of(book));
        when(customerRepository.save(purchase.getCustomer())).thenReturn(savedCustomer);

        PurchaseJson result = checkoutService.placeOrder(purchase);

        assertNotNull(result);
        assertNotNull(result.getOrderTrackingNumber());

        verify(bookRepository).existsById(anyString());
        verify(bookRepository).findById(anyString());
        verify(customerRepository).save(purchase.getCustomer());
    }

    @Test
    void shouldPlaceOrderMethodThrowBookstoreServiceExceptionWhenBookIsNotFound() {
        Purchase purchase = generatePurchase();

        when(bookRepository.existsById(anyString())).thenReturn(false);

        assertThrows(BookStoreServiceException.class, ()->checkoutService.placeOrder(purchase));

        verify(bookRepository).existsById(anyString());
    }

    @Test
    void shouldPlaceOrderMethodThrowBookstoreServiceExceptionWhenQuantityIsLowerThanNumberOfAvailableBookUnits() {
        Purchase purchase = generatePurchase();
        purchase.getOrderItems().stream().iterator().forEachRemaining(orderItem -> orderItem.setQuantity(20));

        Book book = BookGenerator.generateBookWithId(LocalDateTime.now(), "bookId");
        book.setUnitsInStock(15);

        when(bookRepository.existsById(anyString())).thenReturn(true);
        when(bookRepository.findById(anyString())).thenReturn(Optional.of(book));

        assertThrows(BookStoreServiceException.class, ()->checkoutService.placeOrder(purchase));

        verify(bookRepository).existsById(anyString());
        verify(bookRepository).findById(anyString());
    }

    @Test
    void shouldPlaceOrderMethodThrowBookStoreServiceExceptionWhenNonTransientDataAccessExceptionOccur() {
        Purchase purchase = generatePurchase();
        Book book = BookGenerator.generateBookWithId(LocalDateTime.now(), "bookId");

        when(bookRepository.existsById(anyString())).thenReturn(true);
        when(bookRepository.findById(anyString())).thenReturn(Optional.of(book));
        doThrow(new NonTransientDataAccessException(""){}).when(customerRepository).save(purchase.getCustomer());

        assertThrows(BookStoreServiceException.class, () -> checkoutService.placeOrder(purchase));

        verify(bookRepository).existsById(anyString());
        verify(bookRepository).findById(anyString());
        verify(customerRepository).save(purchase.getCustomer());
    }
}
