package com.market.main.emarket.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable @NoArgsConstructor
@AllArgsConstructor
@Getter
public class ProductLikeId implements Serializable {
    private Long userId;
    private Long productId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductLikeId)) return false;
        ProductLikeId that = (ProductLikeId) o;
        return Objects.equals(userId, that.userId)
                && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, productId);
    }
}



