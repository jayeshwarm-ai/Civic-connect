package com.civicconnect.citizen.dto.response;

import com.civicconnect.citizen.enums.Gender;
import com.civicconnect.citizen.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CitizenResponse {

    private Long        citizenId;
    private Long        userId;
    private String      name;
    private LocalDate   dob;
    private Gender      gender;
    private String      address;
    private String      contactInfo;
    private String      email;
    private String      phone;
    private UserStatus  accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
