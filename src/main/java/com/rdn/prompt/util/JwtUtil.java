package com.rdn.prompt.util;

import com.rdn.prompt.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class JwtUtil {
    private JwtUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Key SECRET = JwtKeyGenerator.generateSecretKey();

    private static final long EXPIRATION_TIME = 60 * 60 * 1000;




    public static String generateToken(User user) {
        Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
        Date now = new Date();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getId())
                .claim("name", user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }


    public static Claims verifyToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
