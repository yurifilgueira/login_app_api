package com.yuri.filgueira.login_app_api.services;

import com.yuri.filgueira.login_app_api.entities.model.User;
import com.yuri.filgueira.login_app_api.entities.vos.*;
import com.yuri.filgueira.login_app_api.repositories.UserRepository;
import com.yuri.filgueira.login_app_api.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class AuthServices {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserRepository userRepository;
    private Logger logger = Logger.getLogger(AuthServices.class.getName());

    public ResponseEntity<LoginResponseVO> signin(AccountCredentialsVO data) {
        try {
            var email = data.email();
            var password = data.password();

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            var user = userRepository.findByEmail(email);
            var tokenVO = new TokenVO();
            var loginResponseVO = new LoginResponseVO();

            if (user != null) {

                UserVO userVO = new UserVO();
                userVO.setId(user.getId());
                userVO.setEmail(user.getEmail());
                userVO.setName(user.getName());

                tokenVO = jwtTokenProvider.createAccessToken(email, user.getRoles());
                tokenVO.setUserId(user.getId());
                loginResponseVO = new LoginResponseVO(
                        userVO,
                        tokenVO.getAccessToken(),
                        tokenVO.getRefreshToken(),
                        tokenVO.getExpiration(),
                        tokenVO.getRefreshTokenExpiration());
            }else {
                throw new UsernameNotFoundException("Email " + email + " not found.");
            }

            return ResponseEntity.ok(loginResponseVO);
        }catch (Exception e) {
            throw new BadCredentialsException("Invalid email or password.");
        }
    }

    public ResponseEntity<User> register(RegisterAccountCredentialsVO data) {

        Map<String, PasswordEncoder> encoders = new HashMap<>();

        Pbkdf2PasswordEncoder pbkdf2Encoder = new Pbkdf2PasswordEncoder("", 8, 185000, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        encoders.put("pbkdf2", pbkdf2Encoder);
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("pbkdf2", encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(pbkdf2Encoder);

        var name = data.name();
        var email = data.email();
        var password = passwordEncoder.encode(data.password());
        var roles = data.roles();

        var user = userRepository.save(new User(name, email, password, roles));

        return ResponseEntity.status(HttpStatus.CREATED).body(user);

    }

    public ResponseEntity<TokenVO> refreshToken(String refreshToken) {

        var token = jwtTokenProvider.refreshToken(refreshToken);

        return ResponseEntity.ok(token);

    }
}
