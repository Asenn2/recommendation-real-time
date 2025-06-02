package com.market.main.emarket.repositories;

import com.market.main.emarket.model.Cart;
import com.market.main.emarket.model.Cart;
import com.market.main.emarket.model.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CartRepository extends JpaRepository<Cart, Long> {

    Cart findByUser(MyUser myUser);
}
