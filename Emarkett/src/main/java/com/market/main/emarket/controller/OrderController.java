package com.market.main.emarket.controller;

import com.market.main.emarket.model.MyUser;
import com.market.main.emarket.model.Order;
import com.market.main.emarket.services.OrderService;
import com.market.main.emarket.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping("/order/confirmation/{orderId}")
    public String orderConfirmation(@PathVariable Long orderId,
                                    @AuthenticationPrincipal UserDetails user,
                                    Model model) {
        MyUser currentUser = userService.getUserByUsername(user.getUsername());
        Order order = orderService.getOrderByIdAndUser(orderId, currentUser.getId());

        model.addAttribute("order", order);
        return "order-confirmation";
    }
}