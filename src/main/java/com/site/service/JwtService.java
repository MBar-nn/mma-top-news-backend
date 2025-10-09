package com.site.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final Key key;
    private final long accessExpSec;
    private final long refreshExpSec;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.accessTokenExpirationSec}") long accessExpSec,
            @Value("${app.security.jwt.refreshTokenExpirationSec}") long refreshExpSec
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpSec = accessExpSec;
        this.refreshExpSec = refreshExpSec;
    }

    public String generateAccessToken(Long userId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessExpSec)))
                .claim("roles", roles)
                .claim("typ", "access")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshExpSec)))
                .claim("typ", "refresh")
                .claim("createdAt", now.toString())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public boolean isTokenValid(String token, String expectedType) {
        try {
            Jws<Claims> jws = parseToken(token);
            String typ = jws.getBody().get("typ", String.class);
            return expectedType.equals(typ) && jws.getBody().getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

