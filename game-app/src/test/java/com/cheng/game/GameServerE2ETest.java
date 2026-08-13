package com.cheng.game;

import com.cheng.game.network.config.NettyServerProperties;
import com.cheng.game.protocol.ChatMessage;
import com.cheng.game.protocol.LoginResponse;
import com.cheng.game.support.FreePorts;
import com.cheng.game.support.TcpGameClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class GameServerE2ETest {

    private static final int TCP_PORT = FreePorts.next();
    private static final int WS_PORT = FreePorts.next();

    @Container
    @SuppressWarnings("resource")
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.4"))
            .withDatabaseName("game")
            .withUsername("game")
            .withPassword("game");

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("game.netty.tcp-port", () -> TCP_PORT);
        registry.add("game.netty.websocket-port", () -> WS_PORT);
        registry.add("game.security.jwt-secret", () -> "test-secret-key-at-least-32-bytes-long!!");
        registry.add("game.security.ops-token", () -> "test-ops-token");
    }

    @LocalServerPort
    int httpPort;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    NettyServerProperties nettyServerProperties;

    @Test
    void registerLoginTcpChatAndOpsOnline() throws Exception {
        String username = "u_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String password = "secret12";

        JsonNode register = postJson("/api/auth/register", Map.of(
                "username", username,
                "password", password,
                "nickname", "Nick-" + username));
        assertEquals(0, register.path("code").asInt());
        String token = register.path("data").path("token").asText();
        assertTrue(token.length() > 20);

        JsonNode login = postJson("/api/auth/login", Map.of(
                "username", username,
                "password", password));
        assertEquals(0, login.path("code").asInt());
        token = login.path("data").path("token").asText();

        try (TcpGameClient client = new TcpGameClient("127.0.0.1", nettyServerProperties.getTcpPort())) {
            LoginResponse loginResponse = client.login(token);
            assertEquals(0, loginResponse.getCode());
            assertEquals("Nick-" + username, loginResponse.getNickname());

            assertTrue(client.heartbeat().getServerTime() > 0);

            client.sendChat("hello-e2e");
            ChatMessage chat = client.readChat();
            assertEquals("hello-e2e", chat.getContent());
            assertEquals("Nick-" + username, chat.getNickname());

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Ops-Token", "test-ops-token");
            ResponseEntity<String> online = restTemplate.exchange(
                    "http://localhost:" + httpPort + "/api/ops/online",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
            JsonNode onlineJson = objectMapper.readTree(online.getBody());
            assertEquals(0, onlineJson.path("code").asInt());
            assertTrue(onlineJson.path("data").path("sessionCount").asInt() >= 1);
        }
    }

    @Test
    void debugPageIsPublic() {
        ResponseEntity<String> page = restTemplate.getForEntity(
                "http://localhost:" + httpPort + "/debug.html", String.class);
        assertTrue(page.getStatusCode().is2xxSuccessful());
        assertTrue(page.getBody() != null && page.getBody().contains("WebSocket Debug"));
    }

    private JsonNode postJson(String path, Map<String, Object> body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + httpPort + path,
                new HttpEntity<>(body, headers),
                String.class);
        return objectMapper.readTree(response.getBody());
    }
}
