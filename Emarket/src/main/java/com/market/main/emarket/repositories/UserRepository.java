package com.market.main.emarket.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.market.main.emarket.model.MyUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<MyUser, Long> {
    // Exemples de méthodes personnalisées
    Optional<MyUser> findByUsername(String username);
    MyUser findById(long id);
    void deleteMyUserById(Long id);
}
