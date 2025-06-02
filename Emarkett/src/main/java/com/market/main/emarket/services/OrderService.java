package com.market.main.emarket.services;

import com.market.main.emarket.model.*;
import com.market.main.emarket.repositories.OrderItemRepository;
import com.market.main.emarket.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Transactional
    public Order createOrderFromCart(Long userId) {
        MyUser user = userService.getUserById(userId);
        List<CartItem> cartItems = cartService.getCartItems(userId);

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart");
        }

        // Create the order
        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setTotalAmount(calculateTotal(cartItems));
        order = orderRepository.save(order);

        // Convert cart items to order items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtOrderTime(cartItem.getPriceAtAddition());
            orderItemRepository.save(orderItem);
        }

        // Clear the cart after order is created
        cartService.clearCart(userId);

        return order;
    }

    private double calculateTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToDouble(item -> item.getPriceAtAddition() * item.getQuantity())
                .sum();
    }
    public Order getOrderByIdAndUser(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}