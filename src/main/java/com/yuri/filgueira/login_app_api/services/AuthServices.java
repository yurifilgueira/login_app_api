package com.yuri.filgueira.login_app_api.services;

import com.yuri.filgueira.login_app_api.entities.vos.AccountCredentialsVO;
import com.yuri.filgueira.login_app_api.entities.vos.TokenVO;
import com.yuri.filgueira.login_app_api.repositories.UserRepository;
import com.yuri.filgueira.login_app_api.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthServices {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserRepository userRepository;

    public ResponseEntity signin(AccountCredentialsVO data) {
        try {
            var username = data.email();
            var password = data.password();

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            var user = userRepository.findByEmail(username);

            var tokenResponse = new TokenVO();

            if (user.isPresent()) {
                tokenResponse = jwtTokenProvider.createAccessToken(username, user.get().getRoles());
            }else {
                throw new UsernameNotFoundException("Username " + username + " not found.");
            }

            return ResponseEntity.ok(tokenResponse);
        }catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password.");
        }
    }

}
