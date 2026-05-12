package com.civicconnect.citizen.entity;

import com.civicconnect.citizen.enums.DocType;
import com.civicconnect.citizen.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "citizen_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitizenDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocType docType;

    /** URI to file stored on disk / object storage */
    @Column(nullable = false)
    private String fileUri;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;

    /** Rejection remarks added by officer */
    @Column(length = 500)
    private String remarks;

    /** userId of the officer who reviewed — FK ref to identity-service */
    @Column
    private Long reviewedByUserId;

    @Column
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        uploadedDate        = LocalDateTime.now();
        verificationStatus  = VerificationStatus.PENDING;
    }
}
