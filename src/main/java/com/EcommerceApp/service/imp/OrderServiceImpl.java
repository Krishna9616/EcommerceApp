package com.EcommerceApp.service.imp;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.EcommerceApp.model.Cart;
import com.EcommerceApp.model.OrderAddress;
import com.EcommerceApp.model.OrderRequest;
import com.EcommerceApp.model.ProductOrder;
import com.EcommerceApp.model.UserDtls;
import com.EcommerceApp.repository.CartRepository;
import com.EcommerceApp.repository.ProductOrderRepository;
import com.EcommerceApp.service.OrderService;
import com.EcommerceApp.util.CommonUtil;
import com.EcommerceApp.util.OrderStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductOrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public void saveOrder(Integer userId, OrderRequest orderRequest) throws Exception {


        List<Cart> carts = cartRepository.findByUser_Id(userId);

        if (carts == null || carts.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        UserDtls user = carts.get(0).getUser(); // same user for all cart items

        for (Cart cart : carts) {

            ProductOrder order = new ProductOrder();


            order.setOrderId(UUID.randomUUID().toString());
            order.setOrderDate(LocalDate.now());

            order.setProduct(cart.getProduct());


            Double finalPrice = cart.getProduct().getDiscountPrice() != null
                    ? cart.getProduct().getDiscountPrice()
                    : cart.getProduct().getPrice();

            order.setPrice(finalPrice);
            order.setQuantity(cart.getQuantity());
            order.setUser(cart.getUser());

            order.setStatus(OrderStatus.IN_PROGRESS.getName());
            order.setPaymentType(orderRequest.getPaymentType());


            OrderAddress address = new OrderAddress();
            address.setFirstName(orderRequest.getFirstName());
            address.setLastName(orderRequest.getLastName());
            address.setEmail(orderRequest.getEmail());
            address.setMobileNo(orderRequest.getMobileNo());
            address.setAddress(orderRequest.getAddress());
            address.setCity(orderRequest.getCity());
            address.setState(orderRequest.getState());
            address.setPincode(orderRequest.getPincode());

            order.setOrderAddress(address);

            ProductOrder savedOrder = orderRepository.save(order);


            try {
                commonUtil.sendMailForProductOrder(savedOrder, "success");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        resetCart(user);
    }

    private void resetCart(UserDtls user) {
        cartRepository.deleteByUser(user);
    }

    @Override
    public List<ProductOrder> getOrdersByUser(Integer userId) {

        return orderRepository.findByUser_Id(userId);
    }

    @Override
    public ProductOrder updateOrderStatus(Integer id, String status) {
        Optional<ProductOrder> findById = orderRepository.findById(id);

        if (findById.isPresent()) {
            ProductOrder productOrder = findById.get();
            productOrder.setStatus(status);
            return orderRepository.save(productOrder);
        }

        return null;
    }

    @Override
    public List<ProductOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public ProductOrder getOrdersByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    public org.springframework.data.domain.Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
        return orderRepository.findAll(org.springframework.data.domain.PageRequest.of(pageNo, pageSize));
    }
}
