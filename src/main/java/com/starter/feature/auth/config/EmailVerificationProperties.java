package com.starter.feature.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties(prefix = "app.email-verification")
public record EmailVerificationProperties(
        String baseUrl,
        String subject,
        @DurationUnit(ChronoUnit.HOURS) Duration tokenExpirationHours
) {
    public EmailVerificationProperties {
        if (subject == null || subject.isBlank()) {
            subject = "Confirm your email.";
        }
        if (tokenExpirationHours == null) {
            tokenExpirationHours = Duration.ofHours(24);
        }
    }
}
