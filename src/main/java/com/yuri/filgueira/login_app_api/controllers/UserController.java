package com.yuri.filgueira.login_app_api.controllers;

import com.yuri.filgueira.login_app_api.entities.vos.RegisterAccountCredentialsVO;
import com.yuri.filgueira.login_app_api.entities.vos.UpdateUserRequestVO;
import com.yuri.filgueira.login_app_api.entities.vos.UserVO;
import com.yuri.filgueira.login_app_api.repositories.UserRepository;
import com.yuri.filgueira.login_app_api.services.AuthServices;
import com.yuri.filgueira.login_app_api.services.UserServices;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping(value = "/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthServices authServices;
    @Autowired
    private UserServices userServices;

    @GetMapping(value = "/{id}")
    public ResponseEntity<UserVO> findById(@PathVariable("id") Long id) {
        return userServices.findById(id);
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
        return ResponseEntity.created(URI.create("/users/" + Objects.requireNonNull(user.getBody()).getId()))
                .body("User registered successfully!");
    }

    @Transactional
    @PutMapping
    public ResponseEntity<?> update(@RequestBody UpdateUserRequestVO data) {
        if (dataIsNull(data)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!");
        }

        var updateUserInfoResponse = userServices.update(data);
        return Objects.requireNonNullElseGet(updateUserInfoResponse, () -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request!"));

    }

    private boolean dataIsNull(RegisterAccountCredentialsVO data) {

        if (data == null) return true;
        else if (data.name() == null || data.name().isEmpty()) return true;
        else if (data.email() == null || data.email().isEmpty()) return true;
        else if (data.password() == null || data.password().isEmpty()) return true;
        else if (data.roles() == null || data.roles().isEmpty()) return true;

        return false;
    }

    private boolean dataIsNull(UpdateUserRequestVO data) {

        if (data == null) return true;
        else if (data.id() == null) return true;
        else if (data.name() != null && !data.name().isEmpty()) return false;
        else if (data.email() != null && !data.email().isEmpty()) return false;
        else if (data.newPassword() != null && !data.newPassword().isEmpty()) return false;
        else if (data.confirmNewPassword() != null && !data.confirmNewPassword().isEmpty()) return false;

        return true;
    }
}
