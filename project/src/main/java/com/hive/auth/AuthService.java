package com.hive.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hive.exception.ConflictException;
import com.hive.exception.UnauthorizedException;
import com.hive.security.JwtService;
import com.hive.user.User;
import com.hive.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setOnline(false);
        user = userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getUsername());

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(user.getId(), user.getUsername(), user.getEmail());
        return new AuthResponse(token, userInfo);
    }
}
