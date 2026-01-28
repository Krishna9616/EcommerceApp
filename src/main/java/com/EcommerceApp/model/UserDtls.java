package com.EcommerceApp.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserDtls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String mobileNumber;

    private String email;

    private String address;

    private String city;

    private String state;

    private String pincode;


    @JsonIgnore
    private String password;

    private String profileImage;

    private String role;

    private Boolean isEnable = true;

    private Boolean accountNonLocked = true;


    @JsonIgnore
    private Integer failedAttempt = 0;

    @JsonIgnore
    private Date lockTime;

    @JsonIgnore
    private String resetToken;
}
