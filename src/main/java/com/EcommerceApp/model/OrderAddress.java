package com.EcommerceApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
public class OrderAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 15)
    private String mobileNo;

    @NotBlank(message = "Address is required")
    @Size(max = 200)
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 60)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 60)
    private String state;

    @NotBlank(message = "Pincode is required")
    @Size(min = 4, max = 10)
    private String pincode;
}
