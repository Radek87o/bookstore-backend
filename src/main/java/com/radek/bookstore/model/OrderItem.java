package com.radek.bookstore.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GenericGenerator(name = "order_item_id", strategy = "com.radek.bookstore.model.generator.CustomStringGenerator")
    @GeneratedValue(generator = "order_item_id")
    private String id;

    private String imageUrl;

    @NotNull
    private BigDecimal unitPrice;

    @NotNull
    private Integer quantity;

    @NotNull
    private String bookId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
