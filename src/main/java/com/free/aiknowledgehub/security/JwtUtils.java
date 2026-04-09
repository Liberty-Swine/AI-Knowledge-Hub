package com.free.aiknowledgehub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @Description JWT 工具：生成与解析 token
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@Component
public class JwtUtils {

    /**
     * JWT 签名密钥（建议长度 >= 32）
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * token 过期秒数
     */
    @Value("${jwt.expire-seconds:86400}")
    private long expireSeconds;

    /**
     * 生成 token
     * @param username 用户名
     * @return token
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireSeconds * 1000L);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    /**
     * 解析并校验 token
     * @param token token
     * @return claims
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

