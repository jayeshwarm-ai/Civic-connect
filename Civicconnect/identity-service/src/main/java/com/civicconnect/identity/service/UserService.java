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

    // ── 5. GET MY PROFILE (any signed-in staff role) ──────────────────────────
    StaffResponse getMyProfile(Long userId);

    // ── 6. UPDATE MY PROFILE — self-edit email + phone (NOT for City Admin) ───
    StaffResponse updateMyProfile(Long userId, com.civicconnect.identity.dto.request.UpdateMyProfileRequest request);
}
