package com.yuri.filgueira.login_app_api.controllers;

import com.yuri.filgueira.login_app_api.entities.vos.AccountCredentialsVO;
import com.yuri.filgueira.login_app_api.entities.vos.RegisterAccountCredentialsVO;
import com.yuri.filgueira.login_app_api.repositories.UserRepository;
import com.yuri.filgueira.login_app_api.services.AuthServices;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthServices authServices;
    @Autowired
    private UserRepository userRepository;

    @PostMapping(value = "/signin")
    public ResponseEntity<?> signin(@RequestBody AccountCredentialsVO data) {
        if (dataIsNull(data)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }

        var token = authServices.signin(data);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }
        return token;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<?> register(@RequestBody RegisterAccountCredentialsVO data) {
        if (dataIsNull(data)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }

        var user = authServices.register(data);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }
        return user;
    }

    @PostMapping(value = "/refresh/{email}")
    public ResponseEntity<?> register(@PathVariable("email") String email, @RequestHeader("Authorization") String refreshToken) {
        var user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }

        return authServices.refreshToken(refreshToken);
    }

    private boolean dataIsNull(AccountCredentialsVO data) {

        if (data == null) return true;
        else if (data.email() == null || data.email().isEmpty()) return true;
        else if (data.password() == null || data.password().isEmpty()) return true;

        return false;
    }

    private boolean dataIsNull(RegisterAccountCredentialsVO data) {

        if (data == null) return true;
        else if (data.name() == null || data.name().isEmpty()) return true;
        else if (data.email() == null || data.email().isEmpty()) return true;
        else if (data.password() == null || data.password().isEmpty()) return true;
        else if (data.roles() == null || data.roles().isEmpty()) return true;

        return false;
    }

}
