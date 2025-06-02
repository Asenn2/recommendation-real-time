package com.market.main.emarket.controller;

import com.market.main.emarket.model.CartItem;
import com.market.main.emarket.model.MyUser;
import com.market.main.emarket.model.Order;
import com.market.main.emarket.services.CartService;
import com.market.main.emarket.services.OrderService;
import com.market.main.emarket.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    @Autowired
    private final CartService cartService;

    @Autowired
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(@RequestBody String rawProductId, @AuthenticationPrincipal UserDetails user) {
        Long productId = Long.parseLong(rawProductId.trim());
        MyUser currentUser = userService.getUserByUsername(user.getUsername());
        cartService.addItem(currentUser.getId(), productId);
        int cartItemCount = cartService.getItemCount(currentUser.getId());
        System.out.println(productId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cartItemCount", cartItemCount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/cart")
    @Transactional
    public String viewCart(@AuthenticationPrincipal UserDetails user, Model model) {
        MyUser currentUser = userService.getUserByUsername(user.getUsername());

        // Get cart items for the current user
        var cartItems = cartService.getCartItems(currentUser.getId());
        System.out.println("Controller - Cart Items Count: " + cartItems.size());
        for (CartItem item : cartItems) {
            System.out.println("Item ID: " + item.getId() +
                    ", Product: " + item.getProduct().getName() +
                    ", Quantity: " + item.getQuantity());
        }
        for (var item : cartItems) {
            if (item.getPriceAtAddition() <= 0.0) {
                item.setPriceAtAddition(item.getProduct().getPrice());
            }
        }
        // Calculate totals
        double subtotal = 0.0;
        for (var item : cartItems) {
            // Remove .doubleValue() since getPriceAtAddition() already returns a double
            subtotal += item.getPriceAtAddition() * item.getQuantity();
        }

        double tax = subtotal * 0.0825; // 8.25% tax rate
        double total = subtotal + tax;

        int totalItems = cartItems.stream()
                .mapToInt(item -> item.getQuantity())
                .sum();

        // Add attributes to model
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("tax", tax);
        model.addAttribute("total", total);
        model.addAttribute("totalItems", totalItems);

        return "cart"; // This should match your cart.html file name
    }

    @GetMapping("/cart/raw")
    @ResponseBody
    public List<CartItem> getRawCartItems(@AuthenticationPrincipal UserDetails user) {
        MyUser currentUser = userService.getUserByUsername(user.getUsername());
        return cartService.getCartItems(currentUser.getId());
    }

    // Add this to your CartController
    @Autowired
    private OrderService orderService;

    @PostMapping("/cart/checkout")
    @ResponseBody
    public ResponseEntity<?> checkout(@AuthenticationPrincipal UserDetails user) {
        try {
            MyUser currentUser = userService.getUserByUsername(user.getUsername());
            Order order = orderService.createOrderFromCart(currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", order.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
