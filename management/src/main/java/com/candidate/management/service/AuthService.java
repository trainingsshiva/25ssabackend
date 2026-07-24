package com.candidate.management.service;

import com.candidate.management.config.JwtService;
import com.candidate.management.dto.LoginRequest;
import com.candidate.management.dto.JwtResponse;
import com.candidate.management.entity.User;
import com.candidate.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtResponse authenticate(LoginRequest request) {
    	
      //  authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));
    
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getUsername()));

        String jwtToken = jwtService.generateToken(user);

        return new JwtResponse(jwtToken, user.getUsername(), user.getRole().name());
    }
}
