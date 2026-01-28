package com.EcommerceApp.service;

import com.EcommerceApp.model.Cart;
import java.util.List;

public interface CartService {

    Cart saveCart(Integer productId, Integer userId);

    List<Cart> getCartsByUser(Integer userId);

    Integer getCountCart(Integer userId);

    void updateQuantity(String sy, Integer cid);


    void removeCartItem(Integer cartId);


    void clearCartByUser(Integer userId);
}
