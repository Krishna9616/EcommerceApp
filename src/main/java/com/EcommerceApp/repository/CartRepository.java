package com.EcommerceApp.repository;

import java.util.List;

import com.EcommerceApp.model.Cart;
import com.EcommerceApp.model.UserDtls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import jakarta.transaction.Transactional;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    Cart findByProduct_IdAndUser_Id(Integer productId, Integer userId);

    Integer countByUser_Id(Integer userId);

    List<Cart> findByUser_Id(Integer userId);

    @Transactional
    @Modifying
    void deleteByUser(UserDtls user);
}
