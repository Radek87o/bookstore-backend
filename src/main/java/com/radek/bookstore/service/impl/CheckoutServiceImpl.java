package com.radek.bookstore.service.impl;

import com.radek.bookstore.model.*;
import com.radek.bookstore.model.dto.Purchase;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.model.response.PurchaseJson;
import com.radek.bookstore.repository.*;
import com.radek.bookstore.service.CheckoutService;
import com.radek.bookstore.service.CurrentUserService;
import com.radek.bookstore.service.EmailService;
import com.radek.bookstore.utils.UniqueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final AddressRepository addressRepository;
    private final CurrentUserService currentUserService;
    private final EmailService emailService;

    public CheckoutServiceImpl(CustomerRepository customerRepository,
                               BookRepository bookRepository,
                               OrderRepository orderRepository,
                               OrderItemRepository orderItemRepository,
                               AddressRepository addressRepository,
                               CurrentUserService currentUserService,
                               EmailService emailService) {
        this.customerRepository = customerRepository;
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
        this.currentUserService = currentUserService;
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
            Address orderAddress = extractAddress(purchase.getShippingAddress());
            order.setShippingAddress(orderAddress);
            if(isShippingAddressSameAsBillingAddress(orderAddress, purchase.getBillingAddress())) {
                order.setBillingAddress(orderAddress);
            } else {
                Address billingAddress = extractAddress(purchase.getBillingAddress());
                order.setBillingAddress(billingAddress);
            }
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

    @Override
    public Page<Order> findUserOrders(String email, int page, int size) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            if(!currentUser.getEmail().equalsIgnoreCase(email)) {
                String message = format("Inconsistent email with current user email. Passed email: %s,", email);
                log.info(message);
                throw new BookStoreServiceException(message);
            }
            List<Order> usersOrders = orderRepository.findByCustomer_Email(email);
            PagedListHolder<Order> listHolder = new PagedListHolder<>(usersOrders);
            listHolder.setPage(page);
            listHolder.setPageSize(size);
            return new PageImpl<>(listHolder.getPageList(), PageRequest.of(page, size), usersOrders.size());
        } catch (NonTransientDataAccessException exc) {
            String message = format("Error by attempt to retrieve orders of %s, due to: %s", email, exc.getMessage());
            log.info(message);
            throw new BookStoreServiceException(message);
        }
    }

    @Override
    public List<OrderItem> findOrderItems(String orderId) {
        try {
            if(!orderRepository.existsById(orderId)) {
                String message = format("Cannot find order with id: %s", orderId);
                log.info(message);
                throw new BookStoreServiceException(message);
            }
            return orderItemRepository.findByOrderId(orderId);
        } catch (NonTransientDataAccessException exc) {
            String message = format("Error by attempt to retrieve order items for order with id: %s", orderId);
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
                String message = format("Cannot find book with id: %s", bookId);
                log.info(message);
                throw new BookStoreServiceException(message);
            }
            Optional<Book> bookOptional = bookRepository.findById(bookId);
            Book book = bookOptional.orElseGet(bookOptional::get);
            if(book.getUnitsInStock()<quantity) {
                String message = format("Cannot process purchase for book with id: %s  due to insufficient number of units in stock. Requested quantity: %d, units in stock: %d", bookId, quantity, book.getUnitsInStock());
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

    private Address extractAddress(Address address) {
        Optional<Address> optionalAddress = addressRepository.findByStreetAndCityAndLocationNumberAndZipCodeIgnoreCase(
                address.getStreet(), address.getCity(), address.getLocationNumber(), address.getZipCode());
        if(optionalAddress.isEmpty()) {
            return address;
        }
        return optionalAddress.get();
    }

    private boolean isShippingAddressSameAsBillingAddress(Address orderAddress, Address billingAddress) {
         if(isAddressFieldEqual(orderAddress.getStreet(), billingAddress.getStreet()) &&
            isAddressFieldEqual(orderAddress.getCity(), billingAddress.getCity()) &&
            isAddressFieldEqual(orderAddress.getLocationNumber(), billingAddress.getLocationNumber()) &&
            isAddressFieldEqual(orderAddress.getZipCode(), billingAddress.getZipCode())
         ) {
             return true;
         }
        return false;
    }

    private boolean isAddressFieldEqual(String firstVar, String secondVar) {
        return firstVar.equalsIgnoreCase(secondVar);
    }
}
