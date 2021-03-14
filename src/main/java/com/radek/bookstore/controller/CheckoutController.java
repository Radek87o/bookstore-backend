package com.radek.bookstore.controller;

import com.radek.bookstore.model.dto.Purchase;
import com.radek.bookstore.model.json.PurchaseJson;
import com.radek.bookstore.service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody Purchase purchase) {
        PurchaseJson purchaseJson = checkoutService.placeOrder(purchase);
        return ResponseHelper.createCreatedResponse(purchaseJson);
    }
}
