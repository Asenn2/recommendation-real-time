package com.market.main.emarket.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // "order" est un mot réservé en SQL
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    @Getter
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @Setter
    @Getter
    private MyUser user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Setter
    @Getter
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    @Setter
    @Getter
    private double totalAmount;

    @Column(nullable = false)
    @Setter
    @Getter
    private String status ;

    @CreationTimestamp
    @Setter
    @Getter
    private Instant createdAt;

    @UpdateTimestamp
    @Setter
    @Getter
    private Instant updatedAt;
}

