package dev.tise.ecommerce.security;

import dev.tise.ecommerce.model.User;
import dev.tise.ecommerce.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class JwtTokenGenerator {

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secret.key}")
    private String secret;

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, long expiry) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        var user = optionalUser.get();
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", email);
        claims.put("role", user.getType());

        return Jwts.builder()
                .setIssuer("TISE")
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(secretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        String username = extractUsername(token);
        return (username.equals(expectedUsername) && !isTokenExpired(token));
    }

}
