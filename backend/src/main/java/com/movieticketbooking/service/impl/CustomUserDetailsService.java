package com.movieticketbooking.service.impl;

import com.movieticketbooking.model.User;
import com.movieticketbooking.repository.UserRepository;
import com.movieticketbooking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> opt = userRepository.findByEmail(email);
        if(opt.isEmpty()) {
            throw new UsernameNotFoundException(email);
        }
        return opt.get();
    }
}
