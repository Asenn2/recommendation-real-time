package com.market.main.emarket.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "product_likes")
@Getter
@Setter
public class ProductLike {

    @EmbeddedId
    private ProductLikeId id;

    // On lie id.userId à l'objet MyUser via MapsId("userId")
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private MyUser user;

    // On lie id.productId à l'objet Product via MapsId("productId")
    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @CreationTimestamp
    private Instant likedAt;

    // Constructeur vide requis par JPA
    public ProductLike() {}

    // Constructeur de commodité
    public ProductLike(MyUser user, Product product) {
        this.user = user;
        this.product = product;
        this.id = new ProductLikeId(user.getId(), product.getId());
    }

    // getters et setters...
}
