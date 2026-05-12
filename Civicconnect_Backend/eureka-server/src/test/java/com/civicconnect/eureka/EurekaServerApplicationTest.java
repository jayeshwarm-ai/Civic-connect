package com.civicconnect.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class EurekaServerApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Eureka Server context starts successfully
    }
}
