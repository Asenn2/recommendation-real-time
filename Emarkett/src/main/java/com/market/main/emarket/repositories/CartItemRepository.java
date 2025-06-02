package com.market.main.emarket.repositories;

import com.market.main.emarket.model.Cart;
import com.market.main.emarket.model.CartItem;
import com.market.main.emarket.model.MyUser;
import com.market.main.emarket.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartUser(MyUser user);
    CartItem findByCartAndProduct(Cart cart, Product product);
}


