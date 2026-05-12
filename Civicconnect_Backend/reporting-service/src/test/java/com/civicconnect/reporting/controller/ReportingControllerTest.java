package com.civicconnect.reporting.controller;

import com.civicconnect.reporting.dto.request.GenerateReportRequest;
import com.civicconnect.reporting.dto.response.ReportResponse;
import com.civicconnect.reporting.enums.ReportScope;
import com.civicconnect.reporting.service.ReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportingController Tests")
class ReportingControllerTest {

    @Mock private ReportingService reportingService;

    @InjectMocks
    private ReportingController controller;

    private UsernamePasswordAuthenticationToken authWithUserId(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
        auth.setDetails(userId);
        return auth;
    }

    private ReportResponse sampleReport;

    @BeforeEach
    void setUp() {
        sampleReport = ReportResponse.builder()
                .reportId(1L)
                .scope(ReportScope.REQUEST)
                .build();
    }

    @Test
    @DisplayName("generateReport → 201")
    void generateReport_returns201() {
        GenerateReportRequest req = new GenerateReportRequest();
        when(reportingService.generateReport(eq(req), eq(1L))).thenReturn(sampleReport);

        ResponseEntity<ReportResponse> resp =
                controller.generateReport(req, authWithUserId(1L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(reportingService).generateReport(eq(req), eq(1L));
    }

    @Test
    @DisplayName("getReportById → 200")
    void getReportById_returns200() {
        when(reportingService.getReportById(1L)).thenReturn(sampleReport);

        ResponseEntity<ReportResponse> resp = controller.getReportById(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getReportId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAllReports → 200 newest-first list")
    void getAllReports_returns200() {
        when(reportingService.getAllReports()).thenReturn(List.of(sampleReport));

        ResponseEntity<List<ReportResponse>> resp = controller.getAllReports();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getReportsByScope → 200 filtered")
    void getReportsByScope_returns200() {
        when(reportingService.getReportsByScope(ReportScope.FEEDBACK))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<ReportResponse>> resp =
                controller.getReportsByScope(ReportScope.FEEDBACK);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEmpty();
    }
}
