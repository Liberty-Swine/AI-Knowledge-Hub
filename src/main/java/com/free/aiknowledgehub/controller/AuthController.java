package com.free.aiknowledgehub.controller;

import com.free.aiknowledgehub.common.result.Result;
import com.free.aiknowledgehub.security.JwtUtils;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

/**
 * @Description 鉴权接口（最小可用）：登录获取 JWT
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * JWT 工具
     */
    private final JwtUtils jwtUtils;

    public AuthController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 登录（Demo：内置账号 admin/123456）
     * @param req 登录请求
     * @return JWT token
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest req) {
        if (req == null) {
            return Result.error("参数不能为空");
        }
        // Demo 默认账号：建议后续对接真实用户表与加密存储
        if (!"admin".equals(req.getUsername()) || !"123456".equals(req.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        String token = jwtUtils.generateToken(req.getUsername());
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        return Result.success(resp);
    }

    /**
     * @Description 登录请求体
     */
    @Data
    public static class LoginRequest {
        /**
         * 用户名
         */
        private String username;
        /**
         * 密码
         */
        private String password;
    }

    /**
     * @Description 登录响应体
     */
    @Data
    public static class LoginResponse {
        /**
         * JWT token
         */
        private String token;
    }
}

