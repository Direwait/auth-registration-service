package com.example.auth_registration_service.service;

import com.example.auth_registration_service.security.CustomUserDetails;
import com.example.auth_registration_service.database.model.User;
import com.example.auth_registration_service.database.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var byUserLogin = userRepository.findByUserLogin(username);
        if (byUserLogin.isPresent()) {
            User user = byUserLogin.get();
            return new CustomUserDetails(user);
        } else {
            throw new UsernameNotFoundException(username);
        }
    }

    @Transactional
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User addUser(User user) {
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        return userRepository.saveAndFlush(user);
    }

}
