package com.civicconnect.identity.controller;

import com.civicconnect.identity.dto.response.AuditLogResponse;
import com.civicconnect.identity.dto.response.UserResponse;
import com.civicconnect.identity.enums.AuditAction;
import com.civicconnect.identity.service.IAMService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IAMController Tests")
class IAMControllerTest {

    @Mock private IAMService iamService;

    @InjectMocks
    private IAMController controller;

    private UsernamePasswordAuthenticationToken authWithUserId(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
        auth.setDetails(userId);
        return auth;
    }

    private UserResponse sampleUser;
    private AuditLogResponse sampleAudit;

    @BeforeEach
    void setUp() {
        sampleUser = UserResponse.builder().userId(1L).name("admin").build();
        sampleAudit = AuditLogResponse.builder().auditId(1L).action(AuditAction.USER_REGISTERED).build();
    }

    @Test
    @DisplayName("getMyProfile → 200, delegates with extracted userId")
    void getMyProfile_returns200() {
        when(iamService.getMyProfile(1L)).thenReturn(sampleUser);

        ResponseEntity<UserResponse> resp = controller.getMyProfile(authWithUserId(1L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(iamService).getMyProfile(1L);
    }

    @Test
    @DisplayName("getAllAuditLogs → 200")
    void getAllAuditLogs_returns200() {
        when(iamService.getAllAuditLogs()).thenReturn(List.of(sampleAudit));

        ResponseEntity<List<AuditLogResponse>> resp = controller.getAllAuditLogs();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getByResource → 200")
    void getByResource_returns200() {
        when(iamService.getAuditLogsByResource("USER", "1"))
                .thenReturn(List.of(sampleAudit));

        ResponseEntity<List<AuditLogResponse>> resp = controller.getByResource("USER", "1");

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getByAction → 200")
    void getByAction_returns200() {
        when(iamService.getAuditLogsByAction(AuditAction.USER_REGISTERED))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<AuditLogResponse>> resp =
                controller.getByAction(AuditAction.USER_REGISTERED);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEmpty();
    }

    @Test
    @DisplayName("deactivateUser → 200")
    void deactivateUser_returns200() {
        when(iamService.deactivateUser(eq(2L), eq(1L))).thenReturn(sampleUser);

        ResponseEntity<UserResponse> resp = controller.deactivateUser(2L, authWithUserId(1L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(iamService).deactivateUser(eq(2L), eq(1L));
    }

    @Test
    @DisplayName("getByUser → 200")
    void getByUser_returns200() {
        when(iamService.getAuditLogsByUser(1L)).thenReturn(List.of(sampleAudit));

        ResponseEntity<List<AuditLogResponse>> resp = controller.getByUser(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }
}
