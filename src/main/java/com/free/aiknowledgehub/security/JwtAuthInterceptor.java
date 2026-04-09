package com.free.aiknowledgehub.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @Description JWT 拦截器：校验 Authorization: Bearer <token>
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    /**
     * JWT 工具
     */
    private final JwtUtils jwtUtils;

    public JwtAuthInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (auth == null || auth.isBlank() || !auth.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        String token = auth.substring("Bearer ".length()).trim();
        try {
            Claims claims = jwtUtils.parseClaims(token);
            request.setAttribute("jwtSubject", claims.getSubject());
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}

