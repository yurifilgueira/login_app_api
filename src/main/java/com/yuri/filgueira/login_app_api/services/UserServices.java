package com.yuri.filgueira.login_app_api.services;

import com.yuri.filgueira.login_app_api.entities.vos.LoginResponseVO;
import com.yuri.filgueira.login_app_api.entities.vos.UpdateUserRequestVO;
import com.yuri.filgueira.login_app_api.entities.vos.TokenVO;
import com.yuri.filgueira.login_app_api.entities.vos.UserVO;
import com.yuri.filgueira.login_app_api.exceptions.UserNotFoundException;
import com.yuri.filgueira.login_app_api.repositories.UserRepository;
import com.yuri.filgueira.login_app_api.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class UserServices {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private AuthServices authServices;

    public ResponseEntity<UserVO> findById(long id) {

        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        var userVO = new UserVO(user.getId(), user.getName(), user.getUsername());

        return ResponseEntity.ok(userVO);

    }

    public ResponseEntity<?> update(UpdateUserRequestVO data) {
        var user = userRepository.findById(data.id()).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!data.newPassword().equals(data.confirmNewPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New passwords don't match");
        }

        user.setName(data.name());
        user.setEmail(data.email());

        var password = authServices.encodePassword(data.newPassword());
        user.setPassword(password);

        user = userRepository.save(user);
        try {
            var tokenVO = new TokenVO();
            var loginResponseVO = new LoginResponseVO();

            UserVO userVO = new UserVO();
            userVO.setId(user.getId());
            userVO.setEmail(user.getUsername());
            userVO.setName(user.getName());

            tokenVO = jwtTokenProvider.createAccessToken(user.getUsername(), user.getRoles());
            loginResponseVO = authServices.generateLoginResponseVO(userVO, tokenVO);

            return ResponseEntity.ok().body(loginResponseVO);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid email or password.");
        }
    }
}