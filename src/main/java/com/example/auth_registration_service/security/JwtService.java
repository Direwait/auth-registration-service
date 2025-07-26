package com.example.auth_registration_service.security;

import com.example.auth_registration_service.security.dto.TokenPair;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {

    private final String JWT_SECRET;
    private final int JWT_EXPIRATION;
    private final int REFRESH_EXPIRATION;
    private final SecretKey refreshSecretKey;


    public JwtService() {
        Dotenv dotenv = Dotenv.configure().load();
        this.JWT_SECRET = dotenv.get("JWT_SECRET");
        this.JWT_EXPIRATION = Integer.parseInt(dotenv.get("JWT_EXPIRATION"));
        this.REFRESH_EXPIRATION = Integer.parseInt(dotenv.get("REFRESH_EXPIRATION"));
        this.refreshSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(dotenv.get("REFRESH_SECRET")));
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new JwtException("Invalid JWT token");
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractLogin(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSignInKey())
                .compact();
    }

    public String generateToken(CustomUserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public int extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Integer.class));
    }

    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (userDetails == null) return false;

        String username = extractLogin(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Генерация refresh токена (отдельный ключ)
    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(refreshSecretKey)
                .compact();
    }

    // Валидация refresh токена
    public boolean isRefreshTokenValid(String refreshToken, UserDetails userDetails) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(refreshSecretKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            return claims.getSubject().equals(userDetails.getUsername())
                    && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public TokenPair generateTokenPair(UserDetails userDetails) {
        String accessToken = generateToken((CustomUserDetails) userDetails);
        String refreshToken = generateRefreshToken(userDetails);
        return new TokenPair(accessToken, refreshToken);
    }

    public String extractLoginFromRefreshToken(String refreshToken) {
        return Jwts.parser()
                .verifyWith(refreshSecretKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .getSubject();
    }


    public boolean canRefresh(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(refreshSecretKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
