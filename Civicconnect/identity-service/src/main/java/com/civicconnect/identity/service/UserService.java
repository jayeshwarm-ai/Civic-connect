package com.civicconnect.identity.service;

import com.civicconnect.identity.dto.request.CreateStaffRequest;
import com.civicconnect.identity.dto.response.StaffResponse;
import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;

import java.util.List;

/**
 * Service interface for User/Staff operations.
 */
public interface UserService {
    // ── 1. CREATE STAFF ACCOUNT ───────────────────────────────────────────────
    StaffResponse createStaff(CreateStaffRequest request, Long adminId);

    // ── 1b. PUBLIC STAFF REGISTRATION (for demo) ────────────────────────────
    StaffResponse registerStaffPublic(CreateStaffRequest request);

    // ── 2. GET STAFF BY ROLE ──────────────────────────────────────────────────
    List<StaffResponse> getStaffByRole(Role role);

    // ── 3. GET STAFF BY ID ────────────────────────────────────────────────────
    StaffResponse getStaffById(Long userId);

    // ── 4. UPDATE STAFF STATUS ────────────────────────────────────────────────
    StaffResponse updateStaffStatus(Long userId, UserStatus newStatus, Long adminId);
}
