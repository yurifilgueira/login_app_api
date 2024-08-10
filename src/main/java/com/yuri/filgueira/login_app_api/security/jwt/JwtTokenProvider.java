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
import java.util.Base64;
import java.util.Date;
import java.util.Set;

@Service
public class JwtTokenProvider {

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

    public TokenVO createAccessToken(String username, Set<Role> roles) {

        final Date now = new Date();
        final Date validity = new Date(now.getTime() + validityInMilliseconds);
        final String accessToken = getAccessToken(username, roles, now, validity);
        final String refreshToken = getRefreshToken(username, roles, now);

        return new TokenVO(
                username,
                true,
                now,
                validity,
                accessToken,
                refreshToken
        );
    }

    private String getAccessToken(String username, Set<Role> roles, Date now, Date validity) {
        String issuerUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        return JWT.create()
                .withClaim("roles",  roles.stream().toList())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withSubject(username)
                .withIssuer(issuerUrl)
                .sign(algorithm)
                .strip();
    }

    private String getRefreshToken(String username, Set<Role> roles, Date now) {
        return JWT.create()
                .withClaim("roles",  roles.stream().toList())
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + (validityInMilliseconds  * 3)))
                .withSubject(username)
                .sign(algorithm)
                .strip();
    }

    public Authentication getAuthentication(String token) {
        DecodedJWT decodedJWT = decodedJWT(token);

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(decodedJWT.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

    }

    private DecodedJWT decodedJWT(String token) {
        var alg = Algorithm.HMAC256(secretKey.getBytes());
        JWTVerifier verifier = JWT.require(alg).build();

        return verifier.verify(token);
    }

    public String resolverToken(HttpServletRequest request) {

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
        }catch (Exception e) {
            throw new InvalidJwtAuthenticationException("Expired or invalid JWT token!");
        }
    }

}
