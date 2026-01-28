package com.EcommerceApp.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class ProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String orderId;

    private LocalDate orderDate;

    @ManyToOne
    @JsonIgnoreProperties({"description"})  // optional: reduce payload size
    private Product product;

    private Double price;

    private Integer quantity;

    @ManyToOne
    @JsonIgnoreProperties({"password", "resetToken", "failedAttempt"})
    private UserDtls user;

    private String status;

    private String paymentType;

    @OneToOne(cascade = CascadeType.ALL)
    private OrderAddress orderAddress;
}
