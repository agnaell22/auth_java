package com.lima.s.apis.controllers;

import com.lima.s.apis.domain.user.User;
import com.lima.s.apis.dto.LoginRequestDTO;
import com.lima.s.apis.dto.RegisterRequestDTO;
import com.lima.s.apis.dto.ResponseDTO;
import com.lima.s.apis.infra.security.TokenService;
import com.lima.s.apis.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

@PostMapping("/login")
public ResponseEntity login(@RequestBody LoginRequestDTO body) {

    System.out.println("EMAIL RECEBIDO: [" + body.email() + "]");
    System.out.println("SENHA RECEBIDA: [" + body.password() + "]");

    User user = this.repository.findByEmail(body.email())
            .orElseThrow(() -> new RuntimeException("User not found"));

    System.out.println("USUARIO ENCONTRADO: " + user.getEmail());
    System.out.println("HASH NO BANCO: " + user.getPassword());

    boolean senhaCorreta =
            passwordEncoder.matches(body.password(), user.getPassword());

    System.out.println("SENHA CORRETA: " + senhaCorreta);

    if (senhaCorreta) {
        String token = this.tokenService.generateToken(user);

        System.out.println("TOKEN GERADO COM SUCESSO");

        return ResponseEntity.ok(
                new ResponseDTO(user.getName(), token)
        );
    }

    System.out.println("SENHA INCORRETA");

    return ResponseEntity.badRequest().build();
}

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO body){
        Optional<User> user = this.repository.findByEmail(body.email());

        if(user.isEmpty()) {
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setEmail(body.email());
            newUser.setName(body.name());
            this.repository.save(newUser);

            String token = this.tokenService.generateToken(newUser);
            return ResponseEntity.ok(new ResponseDTO(newUser.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }
}
