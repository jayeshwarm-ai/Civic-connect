package com.civicconnect.servicerequest.controller;

import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import com.civicconnect.servicerequest.enums.ServiceRequestType;
import com.civicconnect.servicerequest.repository.ServiceRequestRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal-only stats endpoint for reporting-service.
 * NOT in Swagger. NOT behind JWT. Internal cluster access only.
 */
@Hidden
@RestController
@RequestMapping("/internal/service-requests/stats")
@RequiredArgsConstructor
public class ServiceRequestStatsController {

    private final ServiceRequestRepository serviceRequestRepository;

    @GetMapping
    public ResponseEntity<ServiceRequestStatsResponse> getStats() {
        return ResponseEntity.ok(ServiceRequestStatsResponse.builder()
                .total(serviceRequestRepository.count())
                .submitted(serviceRequestRepository.countByStatus(ServiceRequestStatus.SUBMITTED))
                .assigned(serviceRequestRepository.countByStatus(ServiceRequestStatus.ASSIGNED))
                .inProgress(serviceRequestRepository.countByStatus(ServiceRequestStatus.IN_PROGRESS))
                .resolved(serviceRequestRepository.countByStatus(ServiceRequestStatus.RESOLVED))
                .closed(serviceRequestRepository.countByStatus(ServiceRequestStatus.CLOSED))
                .road(serviceRequestRepository.countByType(ServiceRequestType.ROAD))
                .water(serviceRequestRepository.countByType(ServiceRequestType.WATER))
                .electricity(serviceRequestRepository.countByType(ServiceRequestType.ELECTRICITY))
                .build());
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ServiceRequestStatsResponse {
        private long total;
        private long submitted;
        private long assigned;
        private long inProgress;
        private long resolved;
        private long closed;
        private long road;
        private long water;
        private long electricity;
    }
}
