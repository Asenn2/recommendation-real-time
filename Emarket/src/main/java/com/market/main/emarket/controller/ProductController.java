package com.market.main.emarket.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.main.emarket.model.MyUser;
import com.market.main.emarket.model.Product;
import com.market.main.emarket.repositories.ProductRepository;
import com.market.main.emarket.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecommendationService recommendationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserActionLogger userActionLogger;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/home")
    public String search(
            @RequestParam(value = "q", defaultValue = "") String searchQuery,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "sort", defaultValue = "relevance") String sort,
            Model model,@AuthenticationPrincipal UserDetails user) throws Exception {

        // Logique de recherche
        Page<Product> products = productService.search(searchQuery, page, sort);

        MyUser currentUser = userService.getUserByUsername(user.getUsername());
        int cartItemCount = cartService.getItemCount(currentUser.getId());
        List<Product> recommended=recommendationService.getRecommendedProductsIds(String.valueOf(currentUser.getId()));
        model.addAttribute("recommendedProducts", recommended);
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
            int isLiked = productService.getProductLikeCount(productId);

            UserActionLogger.logAction(String.valueOf(currentUser.getId()),String.valueOf(productId),"click");

            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                List<String> featureList = objectMapper.readValue(
                        product.getFeatures(), new TypeReference<List<String>>() {}
                );


                // Ajouter le produit au modèle
                model.addAttribute("product", product);
                model.addAttribute("cartItemCount",cartItemCount);
                model.addAttribute("features", featureList);
                model.addAttribute("productLiked", isLiked);


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
}
