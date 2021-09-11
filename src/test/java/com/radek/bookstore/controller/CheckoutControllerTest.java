package com.radek.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radek.bookstore.generators.*;
import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.dto.AuthorDto;
import com.radek.bookstore.model.dto.BookDto;
import com.radek.bookstore.model.dto.Purchase;
import com.radek.bookstore.model.response.PurchaseJson;
import com.radek.bookstore.security.filter.JwtAccessDeniedHandler;
import com.radek.bookstore.security.filter.JwtAuthenticationEntryPoint;
import com.radek.bookstore.security.utility.JwtTokenProvider;
import com.radek.bookstore.service.CheckoutService;
import com.radek.bookstore.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.radek.bookstore.generators.AddressGenerator.*;
import static com.radek.bookstore.generators.CreditCardGenerator.*;
import static com.radek.bookstore.generators.CustomerGenerator.*;
import static com.radek.bookstore.generators.OrderGenerator.generateOrderWithTotalPrice;
import static com.radek.bookstore.generators.OrderGenerator.generateOrderWithTotalQuantity;
import static com.radek.bookstore.generators.OrderItemGenerator.*;
import static com.radek.bookstore.generators.PurchaseGenerator.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = CheckoutController.class)
@WithMockUser(username = "user", roles = "ADMIN")
@AutoConfigureMockMvc(addFilters = false)
class CheckoutControllerTest {

    @MockBean
    CheckoutService checkoutService;

    @MockBean
    UserService userService;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockBean
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    @Qualifier("userDetailsService")
    UserDetailsService userDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldPlaceOrderMethodReturnCorrectPurchaseJsonWhenPurchaseIsValid() throws Exception {
        Purchase purchase = PurchaseGenerator.generatePurchase();
        PurchaseJson purchaseResponse = new PurchaseJson("someOrderTrackingNumber");
        when(checkoutService.placeOrder(any(Purchase.class))).thenReturn(purchaseResponse);

        String url = "/api/checkout/purchase";

        mockMvc.perform(post(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(purchase))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(purchaseResponse)));

        verify(checkoutService).placeOrder(any(Purchase.class));
    }

    @ParameterizedTest
    @MethodSource("setOfInvalidPurchaseArguments")
    void shouldSaveBookMethodReturnBadRequestWhenBookDtoIsInvalid(Purchase purchase) throws Exception {
        String url = "/api/checkout/purchase";

        mockMvc.perform(post(url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(purchase))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(checkoutService, never()).placeOrder(purchase);
    }

    private static Stream<Arguments> setOfInvalidPurchaseArguments() {

        return Stream.of(
                Arguments.of(generatePurchaseWithCustomer(generateCustomerWithFirstName(null))),
                Arguments.of(generatePurchaseWithCustomer(generateCustomerWithFirstName(""))),
                Arguments.of(generatePurchaseWithCustomer(generateCustomerWithFirstName("    "))),
                Arguments.of(generatePurchaseWithCustomer(generateCustomerWithLastName(null))),
                Arguments.of(generatePurchaseWithCustomer(generateCustomerWithLastName(""))),
                Arguments.of(generatePurchaseWithCustomer(generateCustomerWithLastName("    "))),
                Arguments.of(generatePurchaseWithCustomer(generateCustomerWithEmail(null))),
                Arguments.of(generatePurchaseWithCustomer(generateCustomerWithEmail("jankowalski@gmail,com"))),
                Arguments.of(generatePurchaseWithCustomer(generateCustomerWithEmail("jankowalski@gmail"))),
                Arguments.of(generatePurchaseWithCustomer(generateCustomerWithEmail("!@#@$%##@!@!"))),
                Arguments.of(generatePurchaseWithAddress(generateAddressWithCity(null))),
                Arguments.of(generatePurchaseWithAddress(generateAddressWithCity(""))),
                Arguments.of(generatePurchaseWithAddress(generateAddressWithCity("    "))),
                Arguments.of(generatePurchaseWithAddress(generateAddressWithStreet(null))),
                Arguments.of(generatePurchaseWithAddress(generateAddressWithStreet(""))),
                Arguments.of(generatePurchaseWithAddress(generateAddressWithStreet("    "))),
                Arguments.of(generatePurchaseWithAddress(generateAddressWithLocationNumber(null))),
                Arguments.of(generatePurchaseWithAddress(generateAddressWithLocationNumber(""))),
                Arguments.of(generatePurchaseWithAddress(generateAddressWithLocationNumber("   "))),
                Arguments.of(generatePurchaseWithAddress(generateAddressWithZipCode(null))),
                Arguments.of(generatePurchaseWithAddress(generateAddressWithZipCode("08400"))),
                Arguments.of(generatePurchaseWithAddress(generateAddressWithZipCode("08--400"))),
                Arguments.of(generatePurchaseWithCreditCard(generateCreditCardWithCardNumber(null))),
                Arguments.of(generatePurchaseWithCreditCard(generateCreditCardWithCardNumber("444433332222111"))),
                Arguments.of(generatePurchaseWithCreditCard(generateCreditCardWithCardNumber(" 4444333322221111"))),
                Arguments.of(generatePurchaseWithCreditCard(generateCreditCardWithSecurityCode(null))),
                Arguments.of(generatePurchaseWithCreditCard(generateCreditCardWithSecurityCode("12"))),
                Arguments.of(generatePurchaseWithCreditCard(generateCreditCardWithSecurityCode("123 "))),
                Arguments.of(generatePurchaseWithCreditCard(generateCreditCardWithSecurityCode("123a"))),
                Arguments.of(generatePurchaseWithCreditCard(generateCreditCardWithExpirationMonth(null))),
                Arguments.of(generatePurchaseWithCreditCard(generateCreditCardWithExpirationMonth(0))),
                Arguments.of(generatePurchaseWithCreditCard(generateCreditCardWithExpirationMonth(13))),
                Arguments.of(generatePurchaseWithCreditCard(generateCreditCardWithExpirationYear(null))),
                Arguments.of(generatePurchaseWithCreditCard(generateCreditCardWithExpirationYear(2020))),
                Arguments.of(generatePurchaseWithOrder(generateOrderWithTotalQuantity(null))),
                Arguments.of(generatePurchaseWithOrder(generateOrderWithTotalQuantity(0))),
                Arguments.of(generatePurchaseWithOrder(generateOrderWithTotalQuantity(-10))),
                Arguments.of(generatePurchaseWithOrder(generateOrderWithTotalPrice(null))),
                Arguments.of(generatePurchaseWithOrder(generateOrderWithTotalPrice(BigDecimal.ZERO))),
                Arguments.of(generatePurchaseWithOrder(generateOrderWithTotalPrice(BigDecimal.valueOf(-1.67)))),
                Arguments.of(generatePurchaseWithOrderItems(generateOrderItemWithTitle(null))),
                Arguments.of(generatePurchaseWithOrderItems(generateOrderItemWithTitle(""))),
                Arguments.of(generatePurchaseWithOrderItems(generateOrderItemWithTitle("   "))),
                Arguments.of(generatePurchaseWithOrderItems(generateOrderItemWithBookId(null))),
                Arguments.of(generatePurchaseWithOrderItems(generateOrderItemWithBookId(""))),
                Arguments.of(generatePurchaseWithOrderItems(generateOrderItemWithBookId("    "))),
                Arguments.of(generatePurchaseWithOrderItems(generateOrderItemWithUnitPrice(null))),
                Arguments.of(generatePurchaseWithOrderItems(generateOrderItemWithUnitPrice(BigDecimal.ZERO))),
                Arguments.of(generatePurchaseWithOrderItems(generateOrderItemWithUnitPrice(BigDecimal.valueOf(1000.00)))),
                Arguments.of(generatePurchaseWithOrderItems(generateOrderItemWithQuantity(0))),
                Arguments.of(generatePurchaseWithOrderItems(generateOrderItemWithQuantity(null))),
                Arguments.of(generatePurchaseWithOrderItems(generateOrderItemWithQuantity(-100)))
        );
    }
}