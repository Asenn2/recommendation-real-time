package com.market.main.emarket.controller;

import com.market.main.emarket.model.MyUser;
import com.market.main.emarket.repositories.UserRepository;
import com.market.main.emarket.services.ProductService;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikeController {

    private final ProductService productService;
    private final UserRepository userRepository;

    @PostMapping("/product/{productId}/like")
    public LikeResponse likeProduct(@PathVariable Long productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Get the username from the authentication
        String username = authentication.getName();

        // Load your MyUser using the username
        MyUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Long userId = user.getId();

        System.out.println("Like request for product " + productId + " by user " + userId);

        boolean isLiked = productService.isProductLikedByUser(productId, userId);
        System.out.println("Current like status: " + isLiked);

        if (isLiked) {
            productService.unlikeProduct(productId, userId);
        } else {
            productService.likeProduct(productId, userId);
        }

        int likeCount = productService.getProductLikeCount(productId);
        System.out.println("New like count: " + likeCount);

        return new LikeResponse(!isLiked, likeCount);
    }

    @Setter
    @Getter
    public static class LikeResponse {
        private boolean liked;
        private int likeCount;

        public LikeResponse(boolean liked, int likeCount) {
            this.liked = liked;
            this.likeCount = likeCount;
        }
    }
}