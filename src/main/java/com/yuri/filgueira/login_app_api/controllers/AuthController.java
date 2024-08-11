package com.yuri.filgueira.login_app_api.controllers;

import com.yuri.filgueira.login_app_api.entities.vos.AccountCredentialsVO;
import com.yuri.filgueira.login_app_api.services.AuthServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthServices authServices;

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

    private boolean dataIsNull(AccountCredentialsVO data) {

        if (data == null) return true;
        else if (data.email() == null || data.email().isEmpty()) return true;
        else if (data.password() == null || data.password().isEmpty()) return true;

        return false;
    }

}
