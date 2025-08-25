package com.mehmetkerem.jwt;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")      private String secret;
    @Value("${jwt.expirationMs:86400000}") private long expirationMs;

    private Key key() {
        // secret en az 256-bit olmalı (Base64 de kullanabilirsin)
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails user) {
        var roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parser().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean isValid(String token, UserDetails user) {
        var claims = parser().parseClaimsJws(token).getBody();
        return user.getUsername().equals(claims.getSubject()) && claims.getExpiration().after(new Date());
    }

    private JwtParser parser() {
        return Jwts.parserBuilder().setSigningKey(key()).build();
    }
}

