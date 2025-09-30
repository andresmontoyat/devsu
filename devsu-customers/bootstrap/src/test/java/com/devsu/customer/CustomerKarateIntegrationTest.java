package com.devsu.customer;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerKarateIntegrationTest extends TestContainerConfig {

    @LocalServerPort
    private int port;

    @BeforeAll
    void beforeAll() {
        System.setProperty("server.port", String.valueOf(port));
        System.setProperty("server.servlet.context-path", "/devsu-customers");
        System.setProperty("server.protocol", "false");
    }

    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:features/customer.feature");
    }
}
