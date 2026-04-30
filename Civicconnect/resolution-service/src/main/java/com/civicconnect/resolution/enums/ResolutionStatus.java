package com.civicconnect.resolution.enums;

public enum ResolutionStatus {
    IN_PROGRESS,  // Resolution created, workflow steps being executed
    COMPLETED     // All workflow steps done — ServiceRequest pushed to RESOLVED
}
