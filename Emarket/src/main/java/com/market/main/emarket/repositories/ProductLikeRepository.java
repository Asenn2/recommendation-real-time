package com.market.main.emarket.repositories;

import com.market.main.emarket.model.ProductLike;
import com.market.main.emarket.model.ProductLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ProductLikeRepository extends JpaRepository<ProductLike, ProductLikeId> {
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ProductLike pl WHERE pl.user.id = :userId AND pl.product.id = :productId")
    void deleteByUserIdAndProductId(Long userId, Long productId);

    int countByProductId(Long productId);
}