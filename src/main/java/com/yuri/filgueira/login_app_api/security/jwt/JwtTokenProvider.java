package com.yuri.filgueira.login_app_api.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.yuri.filgueira.login_app_api.entities.model.Role;
import com.yuri.filgueira.login_app_api.entities.vos.TokenVO;
import com.yuri.filgueira.login_app_api.exceptions.InvalidJwtAuthenticationException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

@Service
public class JwtTokenProvider {

    Logger logger = Logger.getLogger(JwtTokenProvider.class.getName());

    @Value(value = "${security.jwt.token.secret-key:secret}")
    private String secretKey = "secret";

    @Value(value = "${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds = 3600000;

    @Autowired
    private UserDetailsService userDetailsService;

    Algorithm algorithm = null;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        algorithm = Algorithm.HMAC256(secretKey);
    }

    public TokenVO createAccessToken(String email, List<String> roles) {

        final Date now = new Date();
        final Date validity = new Date(now.getTime() + validityInMilliseconds);
        final Date refreshTokenExpiration = new Date(now.getTime() + (validityInMilliseconds  * 3));
        final String accessToken = getAccessToken(email, roles, now, validity);
        final String refreshToken = getRefreshToken(email, roles, now, refreshTokenExpiration);

        return new TokenVO(
                email,
                true,
                now,
                validity,
                accessToken,
                refreshToken,
                refreshTokenExpiration
        );
    }

    public String getAccessToken(String email, List<String> roles, Date now, Date validity) {
        String issuerUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        return JWT.create()
                .withClaim("roles",  roles.stream().toList())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withSubject(email)
                .withIssuer(issuerUrl)
                .sign(algorithm)
                .strip();
    }

    public String getRefreshToken(String email, List<String> roles, Date now, Date refreshTokenExpiration) {
        return JWT.create()
                .withClaim("roles",  roles.stream().toList())
                .withIssuedAt(now)
                .withExpiresAt(refreshTokenExpiration)
                .withSubject(email)
                .sign(algorithm)
                .strip();
    }

    public TokenVO refreshToken(String refreshToken) {

        if (refreshToken.contains("Bearer ")) {
            System.out.println("Refresh token: " + refreshToken);
            refreshToken = refreshToken.replace("Bearer ", "");
        }

        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJwt = verifier.verify(refreshToken);
        String email = decodedJwt.getSubject();
        List<String> roles = decodedJwt.getClaim("roles").asList(String.class);

        return createAccessToken(email, roles);
    }

    public Authentication getAuthentication(String token) {
        DecodedJWT decodedJWT = decodedJWT(token);

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(decodedJWT.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

    }

    public DecodedJWT decodedJWT(String token) {
        var alg = Algorithm.HMAC256(secretKey.getBytes());
        JWTVerifier verifier = JWT.require(alg).build();

        return verifier.verify(token);
    }

    public String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring("Bearer ".length());
        }

        return null;
    }

    public boolean validateToken(String token) throws InvalidJwtAuthenticationException {
        DecodedJWT decodedJWT = decodedJWT(token);
        try {
            return !decodedJWT.getExpiresAt().before(new Date());
        } catch (Exception e) {
            throw new InvalidJwtAuthenticationException("Expired or invalid JWT token!");
        }
    }
}