package com.market.main.emarket.services;

import com.market.main.emarket.controller.CartController;
import com.market.main.emarket.model.Cart;
import com.market.main.emarket.model.CartItem;
import com.market.main.emarket.model.Product;
import com.market.main.emarket.model.MyUser;
import com.market.main.emarket.repositories.CartItemRepository;
import com.market.main.emarket.repositories.CartRepository;
import com.market.main.emarket.repositories.ProductRepository;
import com.market.main.emarket.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    public void addItem(Long userId, Long productId) {
        MyUser user = userService.getUserById(userId);

        // Chercher le panier, ou créer un nouveau si inexistant
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        // Vérifier si l'article existe déjà dans le panier
        CartItem existing = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
            cartItemRepository.save(existing);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(1);
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }

        cartRepository.save(cart);
    }
    public int getItemCount(Long userId) {
        Cart cart = cartRepository.findByUser(userService.getUserById(userId));
        if (cart == null || cart.getItems() == null) return 0;

        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public List<CartItem> getCartItems(Long userId) {
        MyUser user = userService.getUserById(userId);
        System.out.println("CartService: Getting items for user ID: " + userId);

        List<CartItem> items = cartItemRepository.findByCartUser(user);
        System.out.println("CartService: Found " + items.size() + " cart items");

        // Debug each item with detailed info
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            System.out.println("CartService: Item " + (i+1) +
                    " - ID=" + item.getId() +
                    ", Product=" + (item.getProduct() != null ? item.getProduct().getName() : "NULL") +
                    ", Quantity=" + item.getQuantity() +
                    ", Price=" + item.getPriceAtAddition() +
                    ", Product ID=" + (item.getProduct() != null ? item.getProduct().getId() : "NULL"));
        }

        return items;
    }

    public void updateItemQuantity(Long itemId, int quantity, Long userId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        if (quantity <= 0) {
            removeItem(itemId, userId);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
            System.out.println("Updated item " + itemId + " quantity to: " + quantity);
        }
    }

    public void removeItem(Long itemId, Long userId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        cartItemRepository.delete(item);
        System.out.println("Removed cart item with ID: " + itemId);
    }

    public void clearCart(Long userId) {
        MyUser user = userService.getUserById(userId);
        List<CartItem> items = cartItemRepository.findByCartUser(user);

        if (!items.isEmpty()) {
            cartItemRepository.deleteAll(items);
            System.out.println("Cleared " + items.size() + " items from cart for user: " + userId);
        }
    }

    public BigDecimal getCartTotal(Long userId) {
        List<CartItem> items = getCartItems(userId);
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : items) {
            if (item.getPriceAtAddition() != 0 && item.getPriceAtAddition() > 0) {
                BigDecimal itemTotal = BigDecimal.valueOf(item.getPriceAtAddition())
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(itemTotal);
            }
        }

        System.out.println("Cart total for user " + userId + ": " + total);
        return total;
    }

    public boolean isCartEmpty(Long userId) {
        return getCartItems(userId).isEmpty();
    }

}

