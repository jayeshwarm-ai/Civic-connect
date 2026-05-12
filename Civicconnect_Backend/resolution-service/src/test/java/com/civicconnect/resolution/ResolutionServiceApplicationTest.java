package com.civicconnect.resolution;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "jwt.secret=dGhpcy1pcy1hLXZlcnktc2VjdXJlLWtleS1mb3ItY2l2aWNjb25uZWN0LWp3dC0yMDI0",
        "jwt.expiration.ms=86400000"
})
class ResolutionServiceApplicationTest {

    @Test
    void contextLoads() {
        // Verifies the full Spring context starts without errors
    }
}
