package com.market.main.emarket.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.main.emarket.model.MyUser;
import com.market.main.emarket.model.Product;
import com.market.main.emarket.services.CartService;
import com.market.main.emarket.services.ProductService;
import com.market.main.emarket.services.UserService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/home")
    public String search(
            @RequestParam(value = "q", defaultValue = "") String searchQuery,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "sort", defaultValue = "relevance") String sort,
            Model model,@AuthenticationPrincipal UserDetails user) {

        // Logique de recherche
        Page<Product> products = productService.search(searchQuery, page, sort);

        MyUser currentUser = userService.getUserByUsername(user.getUsername());
        int cartItemCount = cartService.getItemCount(currentUser.getId());

        model.addAttribute("cartItemCount",cartItemCount);
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalResults", products.getTotalElements());
        model.addAttribute("sort", sort);

        return "home";
    }
    @GetMapping("/product/{id}")
    public String showProductDetails(@PathVariable("id") Long productId, Model model,@AuthenticationPrincipal UserDetails user) {
        try {
            // Récupérer le produit par son ID
            Optional<Product> productOptional = productService.findById(productId);
            MyUser currentUser = userService.getUserByUsername(user.getUsername());
            int cartItemCount = cartService.getItemCount(currentUser.getId());

            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                List<String> featureList = objectMapper.readValue(
                        product.getFeatures(), new TypeReference<List<String>>() {}
                );
                // Ajouter le produit au modèle
                model.addAttribute("product", product);
                model.addAttribute("cartItemCount",cartItemCount);
                model.addAttribute("features", featureList);


                return "product"; // Nom du template Thymeleaf
            } else {
                // Produit non trouvé - rediriger vers la page d'accueil avec un message
                return "redirect:/home?error=product-not-found";
            }

        } catch (Exception e) {
            // Gérer les erreurs
            System.err.println("Erreur lors de la récupération du produit " + productId + ": " + e.getMessage());
            return "redirect:/home?error=technical-error";
        }
    }

 //   @PostMapping("/like")
   // public ResponseEntity<?> likeProduct(@RequestBody LikeRequest likeRequest,
     //                                    Authentication authentication) {
       // try {            String username = authentication.getName();            ProductLikeResponse response = productService.likeProduct(likeRequest.getProductId(), username);         return ResponseEntity.ok(response);
       // } catch (Exception e) {
         //   return ResponseEntity.badRequest().body(e.getMessage());
      //  }
   // }

    // Request DTO
    public static class LikeRequest {
        private Long productId;

        // getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
    }

    // Response DTO
    @Getter
    public static class ProductLikeResponse {
        // getters and setters
        private int likeCount;

        public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    }
}
