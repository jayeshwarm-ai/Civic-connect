package com.civicconnect.identity.controller;

import com.civicconnect.identity.audit.AuditLogService;
import com.civicconnect.identity.enums.AuditAction;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal endpoint that receives audit log writes from ALL other microservices.
 *
 * Every service (citizen, service-request, resolution, feedback, compliance, reporting,
 * notification) calls POST /internal/audit-logs via their IdentityFeignClient
 * to write a centralised audit trail in identity-service's database.
 *
 * NOT in Swagger. NOT behind JWT. Internal cluster access only.
 */
@Hidden
@Slf4j
@RestController
@RequestMapping("/internal/audit-logs")
@RequiredArgsConstructor
public class AuditLogInternalController {

    private final AuditLogService auditLogService;

    /**
     * Write a single audit log entry.
     * Called by all downstream services after any significant action.
     */
    @PostMapping
    public ResponseEntity<Void> writeAuditLog(@RequestBody AuditLogInternalRequest request) {
        try {
            AuditAction action = AuditAction.valueOf(request.getAction());
            auditLogService.log(
                    request.getPerformedBy(),
                    action,
                    request.getResource(),
                    request.getResourceId(),
                    request.getDetail()
            );
        } catch (IllegalArgumentException e) {
            // Unknown AuditAction — log a warning but don't fail the caller
            log.warn("Unknown AuditAction received: '{}' from service. Detail: {}",
                    request.getAction(), request.getDetail());
        }
        return ResponseEntity.noContent().build();
    }

    // ── Inner DTO ─────────────────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditLogInternalRequest {
        private Long   performedBy;
        private String action;       // AuditAction enum name as String
        private String resource;
        private String resourceId;
        private String detail;
    }
}
