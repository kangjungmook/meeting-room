package com.example.meetingroom.common.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "meetingroom-secret-key-must-be-at-least-32-bytes";
    private static final long EXPIRATION = 1000 * 60 * 60 * 8; // 8시간

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String employeeId, String role, String name, Long userId) {
        return Jwts.builder()
                .setSubject(employeeId)
                .claim("role", role)
                .claim("name", name)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmployeeId(String token) {
        return getClaims(token).getSubject();
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getName(String token) {
        return getClaims(token).get("name", String.class);
    }

    public Long getUserId(String token) {
        Object raw = getClaims(token).get("userId");
        if (raw == null) return null;
        if (raw instanceof Long l) return l;
        if (raw instanceof Integer i) return i.longValue();
        return Long.parseLong(raw.toString());
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
