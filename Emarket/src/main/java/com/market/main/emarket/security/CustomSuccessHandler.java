package com.market.main.emarket.security;

import java.io.IOException;
import java.util.Collection;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        // Récupération des rôles de l'utilisateur
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // URL de redirection par défaut
        String targetUrl = "/";

        // Choix de l'URL selon le rôle
        for (GrantedAuthority auth : authorities) {
            String role = auth.getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                targetUrl = "/admin";
                break;
            } else if (role.equals("ROLE_USER")) {
                targetUrl = "/home";
                break;
            }
            // ajouter d'autres rôles si nécessaire
        }

        // Exécution de la redirection
        response.sendRedirect(request.getContextPath() + targetUrl);
    }



}
