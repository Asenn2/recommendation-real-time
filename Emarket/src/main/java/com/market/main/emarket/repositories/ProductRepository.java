package com.market.main.emarket.repositories;


import com.market.main.emarket.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String trim, String trim1, Pageable pageable);

    List<Product> findByIdIn(List<Long> recommendedIds);
}
