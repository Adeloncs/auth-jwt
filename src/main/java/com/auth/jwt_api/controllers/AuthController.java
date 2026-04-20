package com.auth.jwt_api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.jwt_api.dtos.AuthenticationRequestDTO;
import com.auth.jwt_api.dtos.LoginResponseDTO;
import com.auth.jwt_api.dtos.RegisterRequestDTO;
import com.auth.jwt_api.exceptions.InvalidCredentialsException;
import com.auth.jwt_api.exceptions.UserAlreadyExistsException;
import com.auth.jwt_api.models.User;
import com.auth.jwt_api.repositories.UserRepository;
import com.auth.jwt_api.security.TokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          TokenService tokenService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationRequestDTO request) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.password());
            var authentication = authenticationManager.authenticate(authToken);
            var token = tokenService.generateToken((User) authentication.getPrincipal());
            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        String encryptedPassword = passwordEncoder.encode(request.password());
        User newUser = new User(request.email(), encryptedPassword, request.role());
        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
