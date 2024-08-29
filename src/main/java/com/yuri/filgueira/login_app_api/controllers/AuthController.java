package com.yuri.filgueira.login_app_api.controllers;

import com.yuri.filgueira.login_app_api.entities.vos.AccountCredentialsVO;
import com.yuri.filgueira.login_app_api.entities.vos.RefreshTokenVO;
import com.yuri.filgueira.login_app_api.repositories.UserRepository;
import com.yuri.filgueira.login_app_api.services.AuthServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
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

        var loginResponseVO = authServices.signin(data);
        if (loginResponseVO == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }
        return loginResponseVO;
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<?> register(@RequestBody RefreshTokenVO refreshTokenVO, @RequestHeader("Authorization") String refreshToken) {

        if (refreshTokenVO == null || refreshTokenVO.email() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }

        var user = userRepository.findByEmail(refreshTokenVO.email());

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
}
