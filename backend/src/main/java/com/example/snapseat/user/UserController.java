package com.example.snapseat.user;

import com.example.snapseat.user.dto.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService us;

    public UserController(UserService us) {
        this.us = us;
    }

    @GetMapping("/me")
    public UserResponse getMyProfile(Authentication auth) {
        User u = us.getUserByEmail(auth.getName());

        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail()
        );
    }
}
