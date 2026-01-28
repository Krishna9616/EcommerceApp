package com.EcommerceApp.service.imp;

import java.util.ArrayList;
import java.util.List;

import com.EcommerceApp.model.Cart;
import com.EcommerceApp.model.Product;
import com.EcommerceApp.model.UserDtls;
import com.EcommerceApp.repository.CartRepository;
import com.EcommerceApp.repository.ProductRepository;
import com.EcommerceApp.repository.UserRepository;
import com.EcommerceApp.service.CartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart saveCart(Integer productId, Integer userId) {

        UserDtls userDtls = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));


        Cart cartStatus = cartRepository.findByProduct_IdAndUser_Id(productId, userId);

        Cart cart;


        Double finalPrice = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();

        if (ObjectUtils.isEmpty(cartStatus)) {
            cart = new Cart();
            cart.setProduct(product);
            cart.setUser(userDtls);
            cart.setQuantity(1);
            cart.setTotalPrice(1 * finalPrice);
        } else {
            cart = cartStatus;
            cart.setQuantity(cart.getQuantity() + 1);
            cart.setTotalPrice(cart.getQuantity() * finalPrice);
        }

        return cartRepository.save(cart);
    }

    @Override
    public List<Cart> getCartsByUser(Integer userId) {


        List<Cart> carts = cartRepository.findByUser_Id(userId);

        Double totalOrderPrice = 0.0;
        List<Cart> updateCarts = new ArrayList<>();

        for (Cart c : carts) {

            Double finalPrice = c.getProduct().getDiscountPrice() != null
                    ? c.getProduct().getDiscountPrice()
                    : c.getProduct().getPrice();

            Double totalPrice = finalPrice * c.getQuantity();

            c.setTotalPrice(totalPrice);
            totalOrderPrice = totalOrderPrice + totalPrice;
            c.setTotalOrderPrice(totalOrderPrice);

            updateCarts.add(c);
        }

        return updateCarts;
    }

    @Override
    public Integer getCountCart(Integer userId) {

        return cartRepository.countByUser_Id(userId);
    }

    @Override
    public void updateQuantity(String sy, Integer cid) {

        Cart cart = cartRepository.findById(cid)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        int updateQuantity;

        if ("de".equalsIgnoreCase(sy)) {
            updateQuantity = cart.getQuantity() - 1;

            if (updateQuantity <= 0) {
                cartRepository.delete(cart);
            } else {
                cart.setQuantity(updateQuantity);
                cartRepository.save(cart);
            }
        } else {
            updateQuantity = cart.getQuantity() + 1;
            cart.setQuantity(updateQuantity);
            cartRepository.save(cart);
        }
    }


    @Override
    public void removeCartItem(Integer cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        cartRepository.delete(cart);
    }


    @Override
    public void clearCartByUser(Integer userId) {
        UserDtls user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        cartRepository.deleteByUser(user);
    }
}
