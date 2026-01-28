package com.EcommerceApp.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.EcommerceApp.model.Product;
import com.EcommerceApp.model.UserDtls;
import com.EcommerceApp.service.CartService;
import com.EcommerceApp.service.CategoryService;
import com.EcommerceApp.service.ProductService;
import com.EcommerceApp.service.UserService;
import com.EcommerceApp.util.CommonUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;


    @GetMapping("/home")
    public ResponseEntity<?> homeData() {

        Map<String, Object> res = new HashMap<>();

        res.put("categories",
                categoryService.getAllActiveCategory().stream()
                        .sorted((a, b) -> b.getId().compareTo(a.getId()))
                        .limit(6)
                        .toList());

        res.put("products",
                productService.getAllActiveProducts("").stream()
                        .sorted((a, b) -> b.getId().compareTo(a.getId()))
                        .limit(8)
                        .toList());

        return ResponseEntity.ok(res);
    }


    @GetMapping("/products")
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(defaultValue = "") String ch) {

        Page<Product> page;

        if (ch == null || ch.trim().isEmpty()) {
            page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
        } else {
            page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("products", page.getContent());
        res.put("pageNo", page.getNumber());
        res.put("pageSize", page.getSize());
        res.put("totalPages", page.getTotalPages());
        res.put("totalElements", page.getTotalElements());

        res.put("categoryParam", category);
        res.put("searchParam", ch);

        return ResponseEntity.ok(res);
    }


    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProductById(@PathVariable int id) {

        Product product = productService.getProductById(id);

        if (product == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Product not found"));
        }

        return ResponseEntity.ok(product);
    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {

        if (principal == null) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }

        UserDtls userDtls = userService.getUserByEmail(principal.getName());

        if (userDtls == null) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }

        return ResponseEntity.ok(Map.of(
                "loggedIn", true,
                "user", userDtls,
                "countCart", cartService.getCountCart(userDtls.getId())));
    }


    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(
            @ModelAttribute UserDtls user,
            @RequestParam(value = "img", required = false) MultipartFile file) throws IOException {

        if (userService.existsEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email already exists"));
        }

        if (file != null && !file.isEmpty()) {
            user.setProfileImage(file.getOriginalFilename());
        } else {
            user.setProfileImage("default.jpg");
        }

        UserDtls savedUser = userService.saveUser(user);

        if (!ObjectUtils.isEmpty(savedUser) && file != null && !file.isEmpty()) {

            // ⚠️ Works in dev only; for production store outside jar
            File saveDir = new ClassPathResource("static/img").getFile();

            Path path = Paths.get(saveDir.getAbsolutePath() + File.separator + "profile_img",
                    file.getOriginalFilename());

            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Registered successfully"));
    }


    @PostMapping("/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestParam String email,
            HttpServletRequest request)
            throws UnsupportedEncodingException, MessagingException {

        UserDtls user = userService.getUserByEmail(email);

        if (ObjectUtils.isEmpty(user)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid email"));
        }

        String token = UUID.randomUUID().toString();
        userService.updateUserResetToken(email, token);

        String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + token;
        commonUtil.sendMail(url, email);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password reset link sent to email"));
    }


    @GetMapping("/auth/reset-password")
    public ResponseEntity<?> verifyResetToken(@RequestParam String token) {

        UserDtls user = userService.getUserByToken(token);

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Token invalid or expired"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token valid"));
    }


    @PostMapping("/auth/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String password) {

        UserDtls user = userService.getUserByToken(token);

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Token invalid or expired"));
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setResetToken(null);
        userService.updateUser(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully"));
    }


    @GetMapping("/products/search")
    public ResponseEntity<?> searchProduct(@RequestParam String ch) {
        return ResponseEntity.ok(Map.of(
                "products", productService.searchProduct(ch)));
    }
}
