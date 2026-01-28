package com.EcommerceApp.repository;

import java.util.List;

import com.EcommerceApp.model.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Integer> {

    List<ProductOrder> findByUser_Id(Integer userId);

    ProductOrder findByOrderId(String orderId);
}
