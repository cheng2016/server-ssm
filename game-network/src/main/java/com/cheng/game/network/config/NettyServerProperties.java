package com.cheng.game.network.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "game.netty")
public class NettyServerProperties {

    private boolean enabled = true;
    private int tcpPort = 9000;
    private int websocketPort = 9001;
    private String websocketPath = "/ws";
    private int bossThreads = 1;
    private int workerThreads = 0;
    private int readerIdleSeconds = 90;
    private int businessThreads = 0;
    private int businessQueueCapacity = 1024;
    private int rateLimitWindowSeconds = 1;
    private int heartbeatLimitPerWindow = 5;
    private int chatLimitPerWindow = 3;
    private int loginLimitPerWindow = 10;
    private int loginWindowSeconds = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public void setWebsocketPort(int websocketPort) {
        this.websocketPort = websocketPort;
    }

    public String getWebsocketPath() {
        return websocketPath;
    }

    public void setWebsocketPath(String websocketPath) {
        this.websocketPath = websocketPath;
    }

    public int getBossThreads() {
        return bossThreads;
    }

    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getReaderIdleSeconds() {
        return readerIdleSeconds;
    }

    public void setReaderIdleSeconds(int readerIdleSeconds) {
        this.readerIdleSeconds = readerIdleSeconds;
    }

    public int getBusinessThreads() {
        return businessThreads;
    }

    public void setBusinessThreads(int businessThreads) {
        this.businessThreads = businessThreads;
    }

    public int getBusinessQueueCapacity() {
        return businessQueueCapacity;
    }

    public void setBusinessQueueCapacity(int businessQueueCapacity) {
        this.businessQueueCapacity = businessQueueCapacity;
    }

    public int getRateLimitWindowSeconds() {
        return rateLimitWindowSeconds;
    }

    public void setRateLimitWindowSeconds(int rateLimitWindowSeconds) {
        this.rateLimitWindowSeconds = rateLimitWindowSeconds;
    }

    public int getHeartbeatLimitPerWindow() {
        return heartbeatLimitPerWindow;
    }

    public void setHeartbeatLimitPerWindow(int heartbeatLimitPerWindow) {
        this.heartbeatLimitPerWindow = heartbeatLimitPerWindow;
    }

    public int getChatLimitPerWindow() {
        return chatLimitPerWindow;
    }

    public void setChatLimitPerWindow(int chatLimitPerWindow) {
        this.chatLimitPerWindow = chatLimitPerWindow;
    }

    public int getLoginLimitPerWindow() {
        return loginLimitPerWindow;
    }

    public void setLoginLimitPerWindow(int loginLimitPerWindow) {
        this.loginLimitPerWindow = loginLimitPerWindow;
    }

    public int getLoginWindowSeconds() {
        return loginWindowSeconds;
    }

    public void setLoginWindowSeconds(int loginWindowSeconds) {
        this.loginWindowSeconds = loginWindowSeconds;
    }
}
