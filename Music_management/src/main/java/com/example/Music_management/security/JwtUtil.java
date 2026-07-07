package com.example.Music_management.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 轻量 JWT 工具:签发 / 校验 token,token 中携带 userId(subject)与 userName。
 * 仅用于 Music_management 的用户鉴权,不涉及其他服务。
 */
@Component
public class JwtUtil {

    /** 演示密钥;生产应放环境变量。HMAC-SHA256 需 >=32 字节。 */
    @Value("${jwt.secret:reward-project-demo-secret-key-please-change-in-prod-0123456789}")
    private String secret;

    /** 过期时间:1 天(毫秒)。 */
    @Value("${jwt.expire-ms:86400000}")
    private long expireMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(int userId, String userName) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userName", userName)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMs))
                .signWith(key())
                .compact();
    }

    /** 校验签名与有效期;通过返回 claims,否则返回 null。 */
    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key()).build()
                    .parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validate(String token) {
        return parse(token) != null;
    }

    public Integer getUserId(String token) {
        Claims claims = parse(token);
        return claims == null ? null : Integer.valueOf(claims.getSubject());
    }
}
