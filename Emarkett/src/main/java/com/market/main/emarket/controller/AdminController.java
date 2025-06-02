package com.market.main.emarket.controller;

import com.market.main.emarket.model.MyUser;
import com.market.main.emarket.services.UserActionLogger;
import com.market.main.emarket.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.IntStream;

@Controller
@AllArgsConstructor
public class AdminController {

    @Autowired
    private final UserService userService;


    @GetMapping("/admin")
    public String ManageUsers(Model model,
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "taille", defaultValue = "4") int taille,
                        @RequestParam(name = "keyword", required = false) String keyword,
                              @RequestParam(value = "removed", required = false) String removed
    )
    {
        List<MyUser> myUsers;

        // Si un mot-clé est saisi, faire une recherche
        if (keyword != null && !keyword.isEmpty()) {
            myUsers = userService.searchUsersByUsername(keyword);
            model.addAttribute("keyword", keyword); // pour garder la valeur dans le champ de recherche
        } else {
            myUsers = userService.getAllusers();
        }

        // Pagination
        int totalUsers = myUsers.size();
        int start = page * taille;
        int end = Math.min(start + taille, totalUsers);
        List<MyUser> paginatedMyUsers = (start >= totalUsers) ? List.of() : myUsers.subList(start, end);

        model.addAttribute("listofusers", paginatedMyUsers);
        model.addAttribute("currentPage", page);

        int totalPages = (int) Math.ceil((double) totalUsers / taille);
        int[] pages = IntStream.range(0, totalPages).toArray();
        model.addAttribute("pages", pages);
        if (removed != null) {
            model.addAttribute("removed" , "User Removed Successfully.");
        }
        return "admin";
    }
}
