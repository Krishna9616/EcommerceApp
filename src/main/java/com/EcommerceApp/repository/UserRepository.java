package com.EcommerceApp.repository;

import java.util.List;

import com.EcommerceApp.model.UserDtls;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserDtls, Integer> {

    UserDtls findByEmail(String email);

    List<UserDtls> findByRole(String role);

    UserDtls findByResetToken(String token);

    Boolean existsByEmail(String email);
}
