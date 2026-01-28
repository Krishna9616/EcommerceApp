package com.EcommerceApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Title is required")
    @Size(max = 500)
    @Column(length = 500, nullable = false)
    private String title;

    @Size(max = 5000)
    @Column(length = 5000)
    private String description;


    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be >= 0")
    @Column(nullable = false)
    private Double price;

    @Min(value = 0, message = "Stock must be >= 0")
    @Column(nullable = false)
    private int stock = 0;

    private String image = "default.jpg";

    @Min(value = 0, message = "Discount must be >= 0")
    private int discount = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "discount_price")
    private Double discountPrice;
}
