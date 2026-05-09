package com.civicconnect.servicerequest.enums;

public enum ServiceRequestStatus {
    SUBMITTED,    // Citizen just submitted — awaiting officer assignment
    ASSIGNED,     // Officer assigned by Department Head / Admin
    IN_PROGRESS,  // Officer actively working on it
    RESOLVED,     // Officer marked resolved — citizen must confirm
    CLOSED        // Citizen confirmed — request lifecycle complete
}
