package com.market.main.emarket.controller;

import com.market.main.emarket.model.MyUser;
import com.market.main.emarket.services.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteUser(@PathVariable Long id ) {
        userService.deleteUserById(id);
        return "redirect:/admin?removed";
    }
}
