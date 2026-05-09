package com.civicconnect.gateway;

import com.civicconnect.gateway.filter.JwtAuthFilter;
import com.civicconnect.gateway.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthFilter Tests")
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public path tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should allow login endpoint without token")
    void shouldAllowLoginWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/v1/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(any());
        verify(jwtUtil, never()).validateToken(any());
    }

    @Test
    @DisplayName("Should allow citizen registration without token")
    void shouldAllowCitizenRegistrationWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/v1/citizens/register")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(any());
        verify(jwtUtil, never()).validateToken(any());
    }

    @Test
    @DisplayName("Should allow actuator endpoint without token")
    void shouldAllowActuatorWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(any());
        verify(jwtUtil, never()).validateToken(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Missing / invalid token tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return 401 when Authorization header is missing")
    void shouldReturn401WhenAuthHeaderMissing() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/service-requests")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("Should return 401 when Authorization header does not start with Bearer")
    void shouldReturn401WhenNotBearer() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/citizens/1")
                .header(HttpHeaders.AUTHORIZATION, "Basic sometoken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
    }

    @Test
    @DisplayName("Should return 401 when token is invalid")
    void shouldReturn401WhenTokenInvalid() {
        when(jwtUtil.validateToken("bad-token")).thenReturn(false);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/citizens/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Valid token tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should pass request through and inject identity headers on valid token")
    void shouldPassThroughAndInjectHeadersOnValidToken() {
        String validToken = "valid.jwt.token";
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUserId(validToken)).thenReturn(42L);
        when(jwtUtil.extractEmail(validToken)).thenReturn("citizen@test.com");
        when(jwtUtil.extractRole(validToken)).thenReturn("CITIZEN");

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/service-requests/citizen/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(argThat(ex -> {
            HttpHeaders headers = ex.getRequest().getHeaders();
            return "42".equals(headers.getFirst("X-User-Id"))
                    && "citizen@test.com".equals(headers.getFirst("X-User-Email"))
                    && "CITIZEN".equals(headers.getFirst("X-User-Role"));
        }));
    }

    @Test
    @DisplayName("Filter order should be -100 (highest priority)")
    void filterOrderShouldBeHighestPriority() {
        assertThat(jwtAuthFilter.getOrder()).isEqualTo(-100);
    }
}
