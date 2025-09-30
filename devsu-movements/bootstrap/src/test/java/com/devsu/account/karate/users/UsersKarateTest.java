package com.devsu.account.karate.users;

import com.devsu.account.MainApplication;
import com.devsu.account.TestContainerConfig;
import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MainApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
class UsersKarateTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void beforeEach() {
        System.setProperty("server.port", String.valueOf(port));
    }

    @Karate.Test
    Karate runUsersFeature() {
        return Karate.run("classpath:features/users/users");
    }
}
