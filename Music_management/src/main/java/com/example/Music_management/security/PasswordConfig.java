package com.example.Music_management.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码哈希:BCrypt(每条密码独立随机盐,盐嵌在哈希串里;cost 用默认 10,约 100ms/次,
 * 靠计算慢来对抗离线暴力破解)。库里只存哈希,登录用 matches() 比对,任何地方不再接触明文。
 */
@Configuration
public class PasswordConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
