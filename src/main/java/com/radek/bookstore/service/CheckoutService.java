package com.radek.bookstore.service;

import com.radek.bookstore.model.dto.Purchase;
import com.radek.bookstore.model.json.PurchaseJson;

public interface CheckoutService {

    PurchaseJson placeOrder(Purchase purchase);
}
