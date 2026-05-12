package com.civicconnect.identity.service;

import com.civicconnect.identity.dto.response.AuditLogResponse;
import com.civicconnect.identity.dto.response.UserResponse;
import com.civicconnect.identity.enums.AuditAction;

import java.util.List;

/**
 * Service interface for IAM (Identity and Access Management) operations.
 */
public interface IAMService {
    UserResponse getMyProfile(Long userId);
    List<AuditLogResponse> getAllAuditLogs();
    List<AuditLogResponse> getAuditLogsByUser(Long userId);
    List<AuditLogResponse> getAuditLogsByResource(String resource, String resourceId);
    List<AuditLogResponse> getAuditLogsByAction(AuditAction action);
    UserResponse deactivateUser(Long targetUserId, Long adminId);
}
