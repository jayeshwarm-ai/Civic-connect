package com.civicconnect.identity.controller;

import com.civicconnect.identity.audit.AuditLogService;
import com.civicconnect.identity.enums.AuditAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogInternalController Tests")
class AuditLogInternalControllerTest {

    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private AuditLogInternalController controller;

    @Test
    @DisplayName("writeAuditLog → 204 and delegates to AuditLogService for valid action")
    void writeAuditLog_valid() {
        AuditLogInternalController.AuditLogInternalRequest req =
                new AuditLogInternalController.AuditLogInternalRequest();
        req.setPerformedBy(1L);
        req.setAction("USER_REGISTERED");
        req.setResource("USER");
        req.setResourceId("100");
        req.setDetail("New citizen registered");

        ResponseEntity<Void> resp = controller.writeAuditLog(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(auditLogService).log(
                eq(1L), eq(AuditAction.USER_REGISTERED), eq("USER"), eq("100"),
                eq("New citizen registered"));
    }

    @Test
    @DisplayName("writeAuditLog → 204 and silently skips for unknown action (caller never fails)")
    void writeAuditLog_unknownActionDoesNotFail() {
        AuditLogInternalController.AuditLogInternalRequest req =
                new AuditLogInternalController.AuditLogInternalRequest();
        req.setPerformedBy(1L);
        req.setAction("MADE_UP_ACTION_THAT_DOESNT_EXIST");
        req.setResource("USER");

        ResponseEntity<Void> resp = controller.writeAuditLog(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verifyNoInteractions(auditLogService);
    }
}
