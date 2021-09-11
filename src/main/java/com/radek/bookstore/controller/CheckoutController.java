package com.radek.bookstore.controller;

import com.radek.bookstore.model.Order;
import com.radek.bookstore.model.OrderItem;
import com.radek.bookstore.model.dto.Purchase;
import com.radek.bookstore.model.response.PurchaseJson;
import com.radek.bookstore.service.CheckoutService;
import com.radek.bookstore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

import static com.radek.bookstore.controller.ResponseHelper.createBadRequestResponse;
import static com.radek.bookstore.controller.ResponseHelper.createOkResponse;
import static java.lang.String.format;
import static java.util.Objects.isNull;

@CrossOrigin
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final static Logger log = LoggerFactory.getLogger(UserController.class);

    private final CheckoutService checkoutService;
    private final UserService userService;

    public CheckoutController(CheckoutService checkoutService, UserService userService) {
        this.checkoutService = checkoutService;
        this.userService = userService;
    }

    @PostMapping(path = "/purchase", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> placeOrder(@Valid @RequestBody Purchase purchase) {
        PurchaseJson purchaseJson = checkoutService.placeOrder(purchase);
        return ResponseHelper.createCreatedResponse(purchaseJson);
    }

    @GetMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findUserOrders(@RequestParam("email") String email,
                                            @RequestParam(name="page", required = false) Integer page,
                                            @RequestParam(name="size", required = false) Integer size) {
        if(isNull(page)) {
            page=0;
        }
        if(isNull(size)) {
            size=20;
        }
        if(isNull(userService.findUserByEmail(email))) {
            String message = format("Cannot find user with email: %s", email);
            log.info(message);
            return createBadRequestResponse(message);
        }
        Page<Order> userOrders = checkoutService.findUserOrders(email, page, size);
        return createOkResponse(userOrders);
    }

    @GetMapping(value = "/orders/{orderId}/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findOrderItems(@PathVariable("orderId") String orderId) {
        List<OrderItem> orderItems = checkoutService.findOrderItems(orderId);
        return createOkResponse(orderItems);
    }
}
