package com.EcommerceApp.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.EcommerceApp.model.Cart;
import com.EcommerceApp.model.OrderRequest;
import com.EcommerceApp.model.ProductOrder;
import com.EcommerceApp.model.UserDtls;
import com.EcommerceApp.service.CartService;
import com.EcommerceApp.service.OrderService;
import com.EcommerceApp.service.UserService;
import com.EcommerceApp.util.CommonUtil;
import com.EcommerceApp.util.OrderStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/me")
    public ResponseEntity<?> me(Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        UserDtls user = getLoggedInUserDetails(p);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "user", user,
                "countCart", cartService.getCountCart(user.getId())
        ));
    }


    // React should call: POST /api/user/cart/add?pid=1
    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(@RequestParam Integer pid, Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        UserDtls user = getLoggedInUserDetails(p);

        Cart saveCart = cartService.saveCart(pid, user.getId());

        if (ObjectUtils.isEmpty(saveCart)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Product add to cart failed"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Product added to cart"
        ));
    }


    @GetMapping("/cart")
    public ResponseEntity<?> getCart(Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        UserDtls user = getLoggedInUserDetails(p);

        List<Cart> carts = cartService.getCartsByUser(user.getId());

        Double totalOrderPrice = 0.0;
        if (!carts.isEmpty()) {
            totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "carts", carts,
                "totalOrderPrice", totalOrderPrice
        ));
    }


    // sy = "in" or "de"
    @PutMapping("/cart/quantity")
    public ResponseEntity<?> updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid, Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        cartService.updateQuantity(sy, cid);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cart quantity updated"
        ));
    }


    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request, Principal p) throws Exception {

        if (p == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        UserDtls user = getLoggedInUserDetails(p);

        orderService.saveOrder(user.getId(), request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order placed successfully"
        ));
    }


    @GetMapping("/orders")
    public ResponseEntity<?> myOrders(Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        UserDtls user = getLoggedInUserDetails(p);

        List<ProductOrder> orders = orderService.getOrdersByUser(user.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "orders", orders
        ));
    }


    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam Integer st,
            Principal p
    ) {

        if (p == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        String status = null;
        for (OrderStatus orderSt : OrderStatus.values()) {
            if (orderSt.getId().equals(st)) {
                status = orderSt.getName();
                break;
            }
        }

        if (status == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid status id"
            ));
        }

        ProductOrder updateOrder = orderService.updateOrderStatus(id, status);

        try {
            commonUtil.sendMailForProductOrder(updateOrder, status);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!ObjectUtils.isEmpty(updateOrder)) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Status updated"
            ));
        }

        return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Status not updated"
        ));
    }


    // Note: your service already handles image upload logic.
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@ModelAttribute UserDtls user,
                                           @RequestParam(required = false) org.springframework.web.multipart.MultipartFile img,
                                           Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        UserDtls updated = userService.updateUserProfile(user, img);

        if (ObjectUtils.isEmpty(updated)) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Profile not updated"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile updated",
                "user", updated
        ));
    }


    @PutMapping("/profile/change-password")
    public ResponseEntity<?> changePassword(@RequestParam String newPassword,
                                            @RequestParam String currentPassword,
                                            Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        UserDtls loggedInUser = getLoggedInUserDetails(p);

        boolean matches = passwordEncoder.matches(currentPassword, loggedInUser.getPassword());

        if (!matches) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Current password incorrect"
            ));
        }

        loggedInUser.setPassword(passwordEncoder.encode(newPassword));

        UserDtls updated = userService.updateUser(loggedInUser);

        if (ObjectUtils.isEmpty(updated)) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Password not updated (server error)"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password updated successfully"
        ));
    }


    private UserDtls getLoggedInUserDetails(Principal p) {
        String email = p.getName();
        return userService.getUserByEmail(email);
    }
}
