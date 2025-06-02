package com.market.main.emarket.controller;

import com.market.main.emarket.model.MyUser;
import com.market.main.emarket.services.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/detail")
    public String UserDetails(Model model,@AuthenticationPrincipal UserDetails user){
        model.addAttribute("user", user);
        return "userdetails";
    }
    @PostMapping("/update")
    public String UpdateDetail(@ModelAttribute("user") MyUser myUser){
        userService.updateUser(myUser);
        return "redirect:/home";
    }
}
