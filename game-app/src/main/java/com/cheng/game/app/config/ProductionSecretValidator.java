package com.cheng.game.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
@Profile("prod")
public class ProductionSecretValidator implements ApplicationRunner {

    static final Set<String> WEAK_JWT_SECRETS = Set.of(
            "change-me-to-a-very-long-secret-key-32bytes!",
            "test-secret-key-at-least-32-bytes-long!!"
    );
    static final Set<String> WEAK_OPS_TOKENS = Set.of("dev-ops-token", "test-ops-token");

    private static final Logger log = LoggerFactory.getLogger(ProductionSecretValidator.class);

    private final AppProperties properties;

    public ProductionSecretValidator(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        validate(properties.getJwtSecret(), properties.getOpsToken());
        log.info("Production secrets passed strength checks");
    }

    static void validate(String jwtSecret, String opsToken) {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("prod requires JWT_SECRET of at least 32 bytes");
        }
        if (WEAK_JWT_SECRETS.contains(jwtSecret)) {
            throw new IllegalStateException("prod forbids the documented default JWT_SECRET");
        }
        if (opsToken == null || opsToken.isBlank() || WEAK_OPS_TOKENS.contains(opsToken)) {
            throw new IllegalStateException("prod requires a non-default OPS_TOKEN");
        }
    }
}
