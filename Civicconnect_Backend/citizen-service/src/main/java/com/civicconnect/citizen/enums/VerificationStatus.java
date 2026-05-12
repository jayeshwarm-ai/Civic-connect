package com.civicconnect.citizen.enums;

public enum VerificationStatus {
    PENDING,   // Uploaded, awaiting officer review
    VERIFIED,  // Officer approved
    REJECTED   // Officer rejected — citizen must re-upload
}
