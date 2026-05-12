package com.civicconnect.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.server.native.search-locations=classpath:/config"
})
class ConfigServerApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Config Server context starts successfully
    }
}
