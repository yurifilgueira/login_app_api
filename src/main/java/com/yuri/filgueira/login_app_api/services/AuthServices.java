package com.yuri.filgueira.login_app_api.services;

import com.yuri.filgueira.login_app_api.entities.model.User;
import com.yuri.filgueira.login_app_api.entities.vos.AccountCredentialsVO;
import com.yuri.filgueira.login_app_api.entities.vos.RegisterAccountCredentialsVO;
import com.yuri.filgueira.login_app_api.entities.vos.TokenVO;
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

    public ResponseEntity<TokenVO> signin(AccountCredentialsVO data) {
        try {
            var email = data.email();
            var password = data.password();

            logger.info("Email: " + email);
            logger.info("Password: " + password);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            var user = userRepository.findByEmail(email);
            var tokenResponse = new TokenVO();

            if (user != null) {
                tokenResponse = jwtTokenProvider.createAccessToken(email, user.getRoles());
            }else {
                throw new UsernameNotFoundException("Email " + email + " not found.");
            }

            return ResponseEntity.ok(tokenResponse);
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
