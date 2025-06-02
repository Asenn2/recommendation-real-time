package com.market.main.emarket.services;

import com.market.main.emarket.model.MyUser;
import com.market.main.emarket.model.Product;
import com.market.main.emarket.model.ProductLike;
import com.market.main.emarket.repositories.ProductLikeRepository;
import com.market.main.emarket.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductLikeRepository productLikeRepository;

    public Page<Product> search(String query, int page, String sortBy) {
        Pageable pageable = createPageable(page, sortBy);

        if (query == null || query.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }

        // Recherche dans le nom et la description
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                query.trim(), query.trim(), pageable);
    }

    private Pageable createPageable(int page, String sortBy) {
        Sort sort = createSort(sortBy);
        return PageRequest.of(page, 12, sort); // 12 produits par page
    }

    private Sort createSort(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "price_low":
                return Sort.by(Sort.Direction.ASC, "price");
            case "price_high":
                return Sort.by(Sort.Direction.DESC, "price");
            case "rating":
            case "likes":
                return Sort.by(Sort.Direction.DESC, "likeCount");
            case "name":
                return Sort.by(Sort.Direction.ASC, "name");
            case "brand":
                return Sort.by(Sort.Direction.ASC, "brand");
            case "relevance":
            default:
                return Sort.by(Sort.Direction.DESC, "likeCount")
                        .and(Sort.by(Sort.Direction.ASC, "name"));
        }
    }
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public void likeProduct(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!productLikeRepository.existsByUserIdAndProductId(userId, productId)) {
            MyUser user = new MyUser();
            user.setId(userId);
            productLikeRepository.save(new ProductLike(user, product));
            product.setLikeCount(productLikeRepository.countByProductId(productId));
            productRepository.save(product);
        }
    }

    @Transactional
    public void unlikeProduct(Long productId, Long userId) {
        if (productLikeRepository.existsByUserIdAndProductId(userId, productId)) {
            productLikeRepository.deleteByUserIdAndProductId(userId, productId);
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            product.setLikeCount(productLikeRepository.countByProductId(productId));
            productRepository.save(product);
        }
    }

    public boolean isProductLikedByUser(Long productId, Long userId) {
        return productLikeRepository.existsByUserIdAndProductId(userId, productId);
    }

    public int getProductLikeCount(Long productId) {
        return productLikeRepository.countByProductId(productId);
    }


}
