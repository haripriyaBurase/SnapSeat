package com.example.snapseat.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository ur;

    public UserService(UserRepository ur) {
        this.ur = ur;
    }

    public User getUserByEmail(String email){
        return ur.findByEmail(email)
                .orElseThrow(()->new RuntimeException("User Not Found!"));
    }
}
