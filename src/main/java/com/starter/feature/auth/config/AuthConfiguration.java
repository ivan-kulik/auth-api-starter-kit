package com.starter.feature.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(EmailVerificationProperties.class)
public class AuthConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
