package com.radek.bookstore.service;

import com.radek.bookstore.model.dto.Purchase;
import com.radek.bookstore.model.response.PurchaseJson;

public interface CheckoutService {

    PurchaseJson placeOrder(Purchase purchase);
}
