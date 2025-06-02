package com.market.main.emarket.repositories;

import com.market.main.emarket.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Add custom queries if needed
}