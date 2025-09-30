package com.devsu.account;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(classes = MainApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class UsersKarateIntegrationTest extends TestContainerConfig {

    @LocalServerPort
    private int port;

    @BeforeEach
    void beforeEach() {
        System.setProperty("server.port", String.valueOf(port));
        System.setProperty("server.servlet.context-path", "/devsu-movements");
        System.setProperty("server.protocol", "false");
    }

    @Karate.Test
    Karate runUsersFeature() {
        return Karate.run("classpath:features/users.feature");
    }
}
