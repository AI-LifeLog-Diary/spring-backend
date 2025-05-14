package com.lifelog.diary.security.jwt;

import com.lifelog.diary.domain.enums.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}")String secret) {

        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getUsername(String token) {

        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("username", String.class);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get("username", String.class);
        }
    }

    public Role getRole(String token) {

        return Role.valueOf(Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class));
    }

    public Boolean isExpired(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    public String createJwt(String username, Role role, Long expiredMs) {

        return Jwts.builder()
                .claim("username", username)
                .claim("role", role.name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String createAccessToken(String username, Role role, Long expireTime) {
        return createJwt(username, role, expireTime);
    }

    public String createRefreshToken(String username, Role role, Long expireTime) {
        return createJwt(username, role, expireTime);
    }



}
