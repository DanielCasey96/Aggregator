package uk.casey.utils;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public static String generateToken(UUID userId, String username) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1200_00))                
                .signWith(key)
                .compact();
    }

    public static boolean validateToken(String token) {
    try {
        JwtParser parser = Jwts.parser().setSigningKey(key).build();
        parser.parseClaimsJws(token);
        return true;
    } catch (Exception e) {
        return false;
    }
    }
}
