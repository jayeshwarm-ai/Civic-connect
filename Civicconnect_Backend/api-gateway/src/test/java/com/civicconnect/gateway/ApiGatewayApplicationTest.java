package com.civicconnect.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "jwt.secret=dGhpcy1pcy1hLXZlcnktc2VjdXJlLWtleS1mb3ItY2l2aWNjb25uZWN0LWp3dC0yMDI0"
})
class ApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the API Gateway context starts correctly
    }
}
