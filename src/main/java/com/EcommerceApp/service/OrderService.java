package com.EcommerceApp.service;

import java.util.List;

import com.EcommerceApp.model.OrderRequest;
import com.EcommerceApp.model.ProductOrder;
import org.springframework.data.domain.Page;

public interface OrderService {

    void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception;

    List<ProductOrder> getOrdersByUser(Integer userId);

    ProductOrder updateOrderStatus(Integer id, String status);

    List<ProductOrder> getAllOrders();

    ProductOrder getOrdersByOrderId(String orderId);

    Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize);


    // ProductOrder getOrderById(Integer id);


    // ProductOrder cancelOrder(Integer id);
}
