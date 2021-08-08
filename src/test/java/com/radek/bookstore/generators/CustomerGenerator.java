package com.radek.bookstore.generators;

import com.radek.bookstore.model.Customer;

import java.util.Collections;

import static com.radek.bookstore.generators.OrderGenerator.generateOrder;

public class CustomerGenerator {

    public static Customer generateCustomer() {
        return generateBaseCustomer();
    }

    public static Customer generateCustomerWithFirstName(String firstName) {
        Customer customer = generateBaseCustomer();
        customer.setFirstName(firstName);
        return customer;
    }

    public static Customer generateCustomerWithLastName(String lastName) {
        Customer customer = generateBaseCustomer();
        customer.setLastName(lastName);
        return customer;
    }

    public static Customer generateCustomerWithEmail(String email) {
        Customer customer = generateBaseCustomer();
        customer.setEmail(email);
        return customer;
    }

    private static Customer generateBaseCustomer() {
        Customer customer = new Customer();
        customer.setEmail("radoslaw.ornat@gmail.com");
        customer.setFirstName("Jan");
        customer.setLastName("Kowalski");
        customer.setId("1234567");
        return customer;
    }
}
