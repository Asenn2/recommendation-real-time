package com.market.main.emarket.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String brand;

    @Column(length = 2000)
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String features;

    @Column(nullable = false)
    private Double price;

    @Column(length = 1000)
    private String image;

    @Column(nullable = false)
    private int likeCount = 0;

    @Lob // Utilisé pour indiquer à JPA d’utiliser un champ long (TEXT ou CLOB)
    @Column(columnDefinition = "TEXT")
    private String categories;
}
