package com.market.main.emarket.repositories;

import com.market.main.emarket.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);
}