package com.radek.bookstore.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.*;
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
    @DecimalMin(value="0.0", inclusive = false)
    @DecimalMax(value="999.99")
    private BigDecimal unitPrice;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotBlank
    private String bookId;

    @NotBlank
    private String title;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
