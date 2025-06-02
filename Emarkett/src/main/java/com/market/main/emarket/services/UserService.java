package com.market.main.emarket.services;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.market.main.emarket.repositories.UserRepository;
import com.market.main.emarket.model.MyUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private final List<MyUser> myUsers = new ArrayList<>();

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    // Récupérer tous les noms d'utilisateurs
    public List<MyUser> getAllusers() {
       return userRepository.findAll();
    }


    //  Trouver un utilisateur par ID
    public MyUser getUserById(Long id) {
        Optional<MyUser> user=userRepository.findById(id) ;
    return user.orElse(null);}

    public MyUser getUserByUsername(String username) {

        return userRepository.findByUsername(username).orElse(null);
    }

    // Supprimer un utilisateur
    public void deleteUser(Long id) {
        userRepository.deleteById(id); ;
    }

    public void updateUser(MyUser myUser) {
        myUser.setRole("ROLE_USER");
        userRepository.save(myUser);
    }

    public List<MyUser> searchUsersByUsername(String keyword) {
        return getAllusers().stream()
                .filter(user -> user.getUsername().toLowerCase().startsWith(keyword.toLowerCase()))
                .toList();
    }

    public void registerUser(MyUser myUser) {
        myUser.setRole("ROLE_USER");
        myUser.setPassword(passwordEncoder.encode(myUser.getPassword()));
        userRepository.save(myUser);
    }
    public void deleteUserById(Long id) {
        userRepository.deleteMyUserById(id);
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user by username from the repository
        Optional <MyUser> myUser = userRepository.findByUsername(username);

        if (myUser.isPresent()) {
            var userObj = myUser.get();

            // Return the user with their authorities (roles)
            return User.builder()
                    .username(userObj.getUsername())
                    .password(userObj.getPassword())
                    .authorities(userObj.getRole())  // Assign roles as authorities
                    .build();
        } else {
            throw new UsernameNotFoundException(username);
        }
    }

}
