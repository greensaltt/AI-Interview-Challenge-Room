package com.offerdungeon.auth.service;

import com.offerdungeon.auth.config.JwtProperties;
import com.offerdungeon.auth.model.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = createSigningKey(jwtProperties.getSecret());
    }

    public IssuedToken issueToken(AuthenticatedUser authenticatedUser) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.getAccessTokenTtl());

        String token = Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(authenticatedUser.id()))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("username", authenticatedUser.username())
                .claim("roles", authenticatedUser.roleCodes())
                .signWith(signingKey)
                .compact();

        return new IssuedToken(token, expiresAt);
    }

    public Long parseUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

    private SecretKey createSigningKey(String secret) {
        byte[] secretBytes = resolveSecretBytes(secret);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes long.");
        }
        return Keys.hmacShaKeyFor(secretBytes);
    }

    private byte[] resolveSecretBytes(String secret) {
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            if (decoded.length >= 32) {
                return decoded;
            }
        } catch (RuntimeException ignored) {
            // Fallback to raw string bytes when the configured secret is not base64-encoded.
        }
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    public record IssuedToken(String token, Instant expiresAt) {}
}
