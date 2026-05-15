package com.civicconnect.identity.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Lookup table of pre-defined security questions citizens choose from when
 * setting up password recovery. Seeded at startup; not user-editable.
 */
@Entity
@Table(name = "security_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(nullable = false, unique = true, length = 255)
    private String questionText;
}
