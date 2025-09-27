package org.example.recipeapp.service;

import org.example.recipeapp.domain.User;
import org.example.recipeapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)  {
        User user = userRepository.loadUserByUsername(username);
        if (user == null){
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }
}
