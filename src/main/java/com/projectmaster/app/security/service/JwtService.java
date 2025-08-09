package com.projectmaster.app.security.service;

import com.projectmaster.app.common.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days in milliseconds
    private long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    public UserRole extractUserRole(String token) {
        String roleStr = extractClaim(token, claims -> claims.get("role", String.class));
        return roleStr != null ? UserRole.valueOf(roleStr) : null;
    }

    public UUID extractCompanyId(String token) {
        String companyIdStr = extractClaim(token, claims -> claims.get("companyId", String.class));
        return companyIdStr != null ? UUID.fromString(companyIdStr) : null;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails, UUID userId, UserRole role, UUID companyId) {
        return generateToken(new HashMap<>(), userDetails, userId, role, companyId);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, 
                               UUID userId, UserRole role, UUID companyId) {
        return buildToken(extraClaims, userDetails, userId, role, companyId, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails, UUID userId, UserRole role, UUID companyId) {
        return buildToken(new HashMap<>(), userDetails, userId, role, companyId, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails,
                             UUID userId, UserRole role, UUID companyId, long expiration) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("userId", userId.toString());
        claims.put("role", role.name());
        claims.put("companyId", companyId != null ? companyId.toString() : null);
        claims.put("tokenType", expiration == jwtExpiration ? "ACCESS" : "REFRESH");

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.warn("Token expiration check failed: {}", e.getMessage());
            return true;
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isRefreshToken(String token) {
        try {
            String tokenType = extractClaim(token, claims -> claims.get("tokenType", String.class));
            return "REFRESH".equals(tokenType);
        } catch (Exception e) {
            log.warn("Token type check failed: {}", e.getMessage());
            return false;
        }
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public long getRefreshExpirationTime() {
        return refreshExpiration;
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid: {}", e.getMessage());
            throw e;
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}