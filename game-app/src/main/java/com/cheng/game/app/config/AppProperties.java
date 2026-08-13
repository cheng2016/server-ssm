package com.cheng.game.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "game.security")
public class AppProperties {

    private String jwtSecret = "change-me-to-a-very-long-secret-key-32bytes!";
    private long jwtExpireSeconds = 86400;
    private String opsToken = "dev-ops-token";

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtExpireSeconds() {
        return jwtExpireSeconds;
    }

    public void setJwtExpireSeconds(long jwtExpireSeconds) {
        this.jwtExpireSeconds = jwtExpireSeconds;
    }

    public String getOpsToken() {
        return opsToken;
    }

    public void setOpsToken(String opsToken) {
        this.opsToken = opsToken;
    }
}
