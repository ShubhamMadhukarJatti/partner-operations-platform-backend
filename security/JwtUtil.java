package com.sharkdom.security;

import com.sharkdom.constants.organization.OrgUserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Component
@Getter
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.audience}")
    private String audience;
    @Value("${jwt.issuer}")
    private String issuer;
    @Value("${jwt.access.token.expiration.time}")
    private long accessTokenExpirationTime;
    @Value("${jwt.refresh.token.expiration.time}")
    private long refreshTokenExpirationTime;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(String username, long expirationTime, OrgUserRole role) {
        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("role", role.name());

        return Jwts.builder()
                .claims(additionalClaims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime * 60000))
                .issuer(issuer)
                .audience()
                .add(audience)
                .and()
                .id(UUID.randomUUID().toString())
                .signWith(secretKey)
                .compact();
    }

    public String generateAccessToken(String username, OrgUserRole role) {
        return generateToken(username, accessTokenExpirationTime, role);
    }

    public String generateRefreshToken(String username, OrgUserRole role) {
        return generateToken(username, refreshTokenExpirationTime, role);
    }

    public Boolean validateToken(String token, String username) {
        final Claims claims = extractAllClaims(token);
        final String extractedUsername = claims.getSubject();
        final String actualIssuer = claims.getIssuer();
        final Set<String> actualAudience = claims.getAudience();
        final Date expiration = claims.getExpiration();

        return (extractedUsername.equals(username) &&
                actualIssuer.equals(issuer) &&
                actualAudience.contains(audience) &&
                !isTokenExpired(expiration));
    }

    private Boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public OrgUserRole extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return OrgUserRole.valueOf(claims.get("role", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
