package com.market.main.emarket.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.header.writers.CacheControlHeadersWriter;
import com.market.main.emarket.model.MyUser;
import com.market.main.emarket.repositories.UserRepository;
import com.market.main.emarket.services.UserService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private  final UserService userService;
    private final UserRepository userRepository;

    private final CustomSuccessHandler customSuccessHandler;

    public SecurityConfig(UserService userService, CustomSuccessHandler customSuccessHandler,UserRepository userRepository) {
        this.customSuccessHandler = customSuccessHandler;
        this.userService = userService;
        this.userRepository = userRepository;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customSuccessHandler)
                        .permitAll()
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/login/**").permitAll()
                        .requestMatchers("../../static/css/styleUser.css").permitAll()
                        .requestMatchers("/logout").permitAll()
                        .requestMatchers("/register").permitAll()
                        .requestMatchers("/process_register").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/admin").hasAnyAuthority("ROLE_ADMIN")
                        .requestMatchers("https://cdn.jsdelivr.net/npm/sweetalert2@11.4.24/dist/sweetalert2.min.css").permitAll()
                        .requestMatchers( "https://cdn.jsdelivr.net/npm/sweetalert2@11.4.24/dist/sweetalert2.all.min.js").permitAll()
                        .requestMatchers("https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js").permitAll()
                        .requestMatchers("https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css").permitAll()

                        .requestMatchers("/resources/**", "/css/**", "/js/**","/plugins/**", "/webjars/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/403")
                )
                .userDetailsService(userService)
                .logout(logout -> logout
                        .logoutUrl("/logout")  // Handles logout URL
                        .logoutSuccessUrl("/login?logout")  // Redirect after logout success
                        .invalidateHttpSession(true)  // Invalidate session
                        .clearAuthentication(true)  // Clear authentication details
                        .deleteCookies("JSESSIONID")  // Delete session cookie
                        .permitAll()  // Allow anyone to access logout
                );

        return http.build();
    }


}