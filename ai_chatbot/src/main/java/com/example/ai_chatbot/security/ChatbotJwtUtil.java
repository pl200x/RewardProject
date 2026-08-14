package com.example.ai_chatbot.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/** Validates access tokens issued by Music_management using the shared secret. */
@Component
public class ChatbotJwtUtil {

    @Value("${jwt.secret:}")
    private String secret;

    public Integer getUserId(String token) {
        if (token == null || secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            return null;
        }
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            return Integer.valueOf(claims.getSubject());
        } catch (Exception ignored) {
            return null;
        }
    }
}
