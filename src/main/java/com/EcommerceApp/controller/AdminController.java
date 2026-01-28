package com.EcommerceApp.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.EcommerceApp.model.Category;
import com.EcommerceApp.model.Product;
import com.EcommerceApp.model.ProductOrder;
import com.EcommerceApp.model.UserDtls;
import com.EcommerceApp.service.*;
import com.EcommerceApp.util.CommonUtil;
import com.EcommerceApp.util.OrderStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================
    //  DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Principal p) {
        UserDtls admin = commonUtil.getLoggedInUserDetails(p);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "admin", admin
        ));
    }

    // =========================
    //  CATEGORY APIs
    // =========================

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "categories", page.getContent(),
                "pageNo", page.getNumber(),
                "pageSize", pageSize,
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "isFirst", page.isFirst(),
                "isLast", page.isLast()
        ));
    }

    @PostMapping("/categories")
    public ResponseEntity<?> saveCategory(
            @ModelAttribute Category category,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws IOException {

        String imageName = (file != null && !file.isEmpty()) ? file.getOriginalFilename() : "default.jpg";
        category.setImageName(imageName);

        Boolean exist = categoryService.existCategory(category.getName());
        if (exist) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Category name already exists"
            ));
        }

        Category saved = categoryService.saveCategory(category);

        if (ObjectUtils.isEmpty(saved)) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Category not saved (server error)"
            ));
        }

        // Save image (dev only)
        if (file != null && !file.isEmpty()) {
            File saveFile = new ClassPathResource("static/img").getFile();

            Path path = Paths.get(saveFile.getAbsolutePath()
                    + File.separator + "category_img"
                    + File.separator + file.getOriginalFilename());

            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Category saved successfully",
                "category", saved
        ));
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable int id) {
        Category category = categoryService.getCategoryById(id);

        if (category == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Category not found"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "category", category
        ));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable int id,
            @ModelAttribute Category category,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws IOException {

        Category oldCategory = categoryService.getCategoryById(id);

        if (oldCategory == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Category not found"
            ));
        }

        String imageName = (file != null && !file.isEmpty())
                ? file.getOriginalFilename()
                : oldCategory.getImageName();

        oldCategory.setName(category.getName());
        oldCategory.setIsActive(category.getIsActive());
        oldCategory.setImageName(imageName);

        Category updated = categoryService.saveCategory(oldCategory);

        if (ObjectUtils.isEmpty(updated)) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Category update failed"
            ));
        }

        if (file != null && !file.isEmpty()) {
            File saveFile = new ClassPathResource("static/img").getFile();

            Path path = Paths.get(saveFile.getAbsolutePath()
                    + File.separator + "category_img"
                    + File.separator + file.getOriginalFilename());

            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Category updated successfully",
                "category", updated
        ));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable int id) {
        Boolean deleted = categoryService.deleteCategory(id);

        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Category deleted successfully"
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Category delete failed"
        ));
    }

    // =========================
    //  PRODUCT APIs
    // =========================

    @GetMapping("/products")
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "") String ch,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<Product> page;

        if (ch != null && ch.trim().length() > 0) {
            page = productService.searchProductPagination(pageNo, pageSize, ch);
        } else {
            page = productService.getAllProductsPagination(pageNo, pageSize);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "products", page.getContent(),
                "pageNo", page.getNumber(),
                "pageSize", pageSize,
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "isFirst", page.isFirst(),
                "isLast", page.isLast()
        ));
    }

    @PostMapping("/products")
    public ResponseEntity<?> saveProduct(
            @ModelAttribute Product product,
            @RequestParam(value = "file", required = false) MultipartFile image
    ) throws IOException {

        String imageName = (image != null && !image.isEmpty()) ? image.getOriginalFilename() : "default.jpg";

        product.setImage(imageName);
        product.setDiscount(0);
        product.setDiscountPrice(product.getPrice());

        Product saved = productService.saveProduct(product);

        if (ObjectUtils.isEmpty(saved)) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Product not saved (server error)"
            ));
        }

        if (image != null && !image.isEmpty()) {
            File saveFile = new ClassPathResource("static/img").getFile();

            Path path = Paths.get(saveFile.getAbsolutePath()
                    + File.separator + "product_img"
                    + File.separator + image.getOriginalFilename());

            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Product saved successfully",
                "product", saved
        ));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProductById(@PathVariable int id) {
        Product product = productService.getProductById(id);

        if (product == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Product not found"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "product", product
        ));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable int id,
            @ModelAttribute Product product,
            @RequestParam(value = "file", required = false) MultipartFile image
    ) {

        product.setId(id);

        if (product.getDiscount() < 0 || product.getDiscount() > 100) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid discount (0 to 100 allowed)"
            ));
        }

        Product updated = productService.updateProduct(product, image);

        if (ObjectUtils.isEmpty(updated)) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Product update failed"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Product updated successfully",
                "product", updated
        ));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        Boolean deleted = productService.deleteProduct(id);

        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Product deleted successfully"
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Product delete failed"
        ));
    }

    // =========================
    //  USERS APIs
    // =========================

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestParam Integer type) {

        List<UserDtls> users;

        if (type == 1) {
            users = userService.getUsers("ROLE_USER");
        } else {
            users = userService.getUsers("ROLE_ADMIN");
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "type", type,
                "users", users
        ));
    }

    @PutMapping("/users/status")
    public ResponseEntity<?> updateUserStatus(
            @RequestParam Integer id,
            @RequestParam Boolean status
    ) {

        Boolean updated = userService.updateAccountStatus(id, status);

        if (updated) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Account status updated"
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Account status update failed"
        ));
    }

    // =========================
    //  ORDERS APIs
    // =========================

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "orders", page.getContent(),
                "pageNo", page.getNumber(),
                "pageSize", pageSize,
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "isFirst", page.isFirst(),
                "isLast", page.isLast()
        ));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam Integer st
    ) {

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

        ProductOrder updatedOrder = orderService.updateOrderStatus(id, status);

        try {
            commonUtil.sendMailForProductOrder(updatedOrder, status);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ObjectUtils.isEmpty(updatedOrder)) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Status not updated"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order status updated",
                "order", updatedOrder
        ));
    }

    @GetMapping("/orders/search")
    public ResponseEntity<?> searchOrder(@RequestParam String orderId) {

        if (orderId == null || orderId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "OrderId is required"
            ));
        }

        ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());

        if (ObjectUtils.isEmpty(order)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Incorrect orderId"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "order", order
        ));
    }

    // =========================
    //  ADMIN PROFILE APIs
    // =========================

    @GetMapping("/profile")
    public ResponseEntity<?> profile(Principal p) {
        UserDtls admin = commonUtil.getLoggedInUserDetails(p);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "admin", admin
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @ModelAttribute UserDtls user,
            @RequestParam(value = "img", required = false) MultipartFile img
    ) {
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
                "admin", updated
        ));
    }

    @PutMapping("/profile/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam String newPassword,
            @RequestParam String currentPassword,
            Principal p
    ) {

        UserDtls admin = commonUtil.getLoggedInUserDetails(p);

        boolean matches = passwordEncoder.matches(currentPassword, admin.getPassword());
        if (!matches) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Current password incorrect"
            ));
        }

        admin.setPassword(passwordEncoder.encode(newPassword));
        UserDtls updated = userService.updateUser(admin);

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
}
