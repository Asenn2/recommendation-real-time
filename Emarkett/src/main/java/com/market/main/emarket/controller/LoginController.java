package com.market.main.emarket.controller;

import com.market.main.emarket.model.MyUser;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.market.main.emarket.repositories.UserRepository;
import com.market.main.emarket.services.UserService;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class LoginController {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/login")
    public String ShowLoginForm(@RequestParam(value = "logout", required = false) String logout, Model model){
        model.addAttribute("user", new MyUser()); // Assure-toi que ta classe s'appelle bien "User"
        if (logout != null) {
            model.addAttribute("logout" , "Disconnected Successfully.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new MyUser());
        return "register";
    }

    @PostMapping("process_register")
    public String processRegister(@ModelAttribute("user") MyUser myUser) {
        userService.registerUser(myUser);
        return "redirect:/login";
    }
}
