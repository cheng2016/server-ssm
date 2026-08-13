package com.cheng.game.network.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.cheng.game.network")
@EnableConfigurationProperties(NettyServerProperties.class)
public class GameNetworkAutoConfiguration {
}
