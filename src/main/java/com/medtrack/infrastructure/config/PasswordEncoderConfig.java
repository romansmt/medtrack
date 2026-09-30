package com.medtrack.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Only spring-security-crypto is on the classpath, not spring-boot-starter-security - there is no
// filter chain, no authentication mechanism, no session handling here. This bean exists purely so
// MockEHealthCardAdapter and DemoAccountSeeder can hash/verify patient passwords properly instead of
// storing them in plain text, matching this project's "treat security as if real, even for fake
// data" principle.
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
