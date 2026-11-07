package com.example.snapseat.auth;

import com.example.snapseat.auth.dto.LoginRequest;
import com.example.snapseat.auth.dto.RegisterRequest;
import com.example.snapseat.security.JwtService;
import com.example.snapseat.user.User;
import com.example.snapseat.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    private final UserRepository ur;
    private final PasswordEncoder pe;

    private final JwtService js;

    public AuthService(UserRepository ur, PasswordEncoder pe, JwtService js) {
        this.ur = ur;
        this.pe = pe;
        this.js = js;
    }

    public String register(RegisterRequest req){
        if(ur.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already registered");

        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPasswordHash(pe.encode(req.getPassword()));

        ur.save(u);

        return "Registration successful";
    }

    public String login(LoginRequest req) {

        User u = ur.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!pe.matches(req.getPassword(), u.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return js.generateToken(u.getEmail());
    }
}
