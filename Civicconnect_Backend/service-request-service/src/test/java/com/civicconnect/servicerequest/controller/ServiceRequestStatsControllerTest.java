package com.civicconnect.servicerequest.controller;

import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import com.civicconnect.servicerequest.enums.ServiceRequestType;
import com.civicconnect.servicerequest.repository.ServiceRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceRequestStatsController Tests")
class ServiceRequestStatsControllerTest {

    @Mock private ServiceRequestRepository serviceRequestRepository;

    @InjectMocks
    private ServiceRequestStatsController controller;

    @Test
    @DisplayName("getStats → 200 with all status + type counts")
    void getStats_returnsAggregatedCounts() {
        when(serviceRequestRepository.count()).thenReturn(30L);
        when(serviceRequestRepository.countByStatus(ServiceRequestStatus.SUBMITTED)).thenReturn(5L);
        when(serviceRequestRepository.countByStatus(ServiceRequestStatus.ASSIGNED)).thenReturn(8L);
        when(serviceRequestRepository.countByStatus(ServiceRequestStatus.IN_PROGRESS)).thenReturn(7L);
        when(serviceRequestRepository.countByStatus(ServiceRequestStatus.RESOLVED)).thenReturn(6L);
        when(serviceRequestRepository.countByStatus(ServiceRequestStatus.CLOSED)).thenReturn(4L);
        when(serviceRequestRepository.countByType(ServiceRequestType.ROAD)).thenReturn(12L);
        when(serviceRequestRepository.countByType(ServiceRequestType.WATER)).thenReturn(10L);
        when(serviceRequestRepository.countByType(ServiceRequestType.ELECTRICITY)).thenReturn(8L);

        ResponseEntity<ServiceRequestStatsController.ServiceRequestStatsResponse> resp =
                controller.getStats();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        ServiceRequestStatsController.ServiceRequestStatsResponse body = resp.getBody();
        assertThat(body.getTotal()).isEqualTo(30L);
        assertThat(body.getSubmitted()).isEqualTo(5L);
        assertThat(body.getAssigned()).isEqualTo(8L);
        assertThat(body.getInProgress()).isEqualTo(7L);
        assertThat(body.getResolved()).isEqualTo(6L);
        assertThat(body.getClosed()).isEqualTo(4L);
        assertThat(body.getRoad()).isEqualTo(12L);
        assertThat(body.getWater()).isEqualTo(10L);
        assertThat(body.getElectricity()).isEqualTo(8L);
    }

    @Test
    @DisplayName("getStats → returns zeros when no requests exist")
    void getStats_handlesEmpty() {
        when(serviceRequestRepository.count()).thenReturn(0L);

        ResponseEntity<ServiceRequestStatsController.ServiceRequestStatsResponse> resp =
                controller.getStats();

        assertThat(resp.getBody().getTotal()).isZero();
    }
}
