package com.market.main.emarket.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    @Getter
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    @Setter
    @Getter
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    @Setter
    @Getter
    private Product product;

    @Column(nullable = false)
    @Setter
    @Getter
    private int quantity;

    @Column(nullable = false)
    @Setter
    @Getter
    private double priceAtOrderTime; // prix lors de la commande
}


