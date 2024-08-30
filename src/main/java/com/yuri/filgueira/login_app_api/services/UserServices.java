package com.yuri.filgueira.login_app_api.services;

import com.yuri.filgueira.login_app_api.entities.vos.RequestUpdateUserVO;
import com.yuri.filgueira.login_app_api.entities.vos.UserVO;
import com.yuri.filgueira.login_app_api.exceptions.UserNotFoundException;
import com.yuri.filgueira.login_app_api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserServices {

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<UserVO> findById(long id) {

        var user =  userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        var userVO = new UserVO(user.getId(), user.getName(), user.getEmail());

        return ResponseEntity.ok(userVO);

    }

    public ResponseEntity<?> update(RequestUpdateUserVO data) {
        var user =  userRepository.findById(data.id()).orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setName(data.name());
        user.setEmail(data.email());
        user.setPassword(data.newPassword());

        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}