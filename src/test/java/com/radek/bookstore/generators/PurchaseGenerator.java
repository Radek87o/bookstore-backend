package com.radek.bookstore.generators;

import com.radek.bookstore.model.*;
import com.radek.bookstore.model.dto.Purchase;

import java.util.Collections;

import static com.radek.bookstore.generators.AddressGenerator.generateBaseAddress;
import static com.radek.bookstore.generators.CreditCardGenerator.generateCreditCard;
import static com.radek.bookstore.generators.CustomerGenerator.generateCustomer;
import static com.radek.bookstore.generators.OrderGenerator.generateOrder;
import static com.radek.bookstore.generators.OrderItemGenerator.generateSetOfOrderItems;

public class PurchaseGenerator {

    public static Purchase generatePurchaseWithCustomer(Customer customer) {
        Purchase purchase = generateBasePurchase();
        purchase.setCustomer(customer);
        return purchase;
    }

    public static Purchase generatePurchaseWithAddress(Address address) {
        Purchase purchase = generateBasePurchase();
        purchase.setBillingAddress(address);
        purchase.setShippingAddress(address);
        return purchase;
    }

    public static Purchase generatePurchaseWithCreditCard(CreditCard creditCard) {
        Purchase purchase = generateBasePurchase();
        purchase.setCreditCard(creditCard);
        return purchase;
    }

    public static Purchase generatePurchaseWithOrder(Order order) {
        Purchase purchase = generateBasePurchase();
        purchase.setOrder(order);
        return purchase;
    }

    public static Purchase generatePurchaseWithOrderItems(OrderItem item) {
        Purchase purchase = generateBasePurchase();
        purchase.setOrderItems(Collections.singleton(item));
        return purchase;
    }

    public static Purchase generatePurchase() {
        return generateBasePurchase();
    }

    private static Purchase generateBasePurchase(){
        Purchase purchase = new Purchase();
        purchase.setCustomer(generateCustomer());
        purchase.setCreditCard(generateCreditCard());
        purchase.setShippingAddress(generateBaseAddress());
        purchase.setBillingAddress(generateBaseAddress());
        purchase.setOrder(generateOrder());
        purchase.setOrderItems(generateSetOfOrderItems());
        return purchase;
    }
}
