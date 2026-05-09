package com.civicconnect.reporting;

import com.civicconnect.reporting.dto.request.GenerateReportRequest;
import com.civicconnect.reporting.dto.response.ReportResponse;
import com.civicconnect.reporting.entity.Report;
import com.civicconnect.reporting.enums.ReportScope;
import com.civicconnect.reporting.exception.InvalidOperationException;
import com.civicconnect.reporting.exception.ResourceNotFoundException;
import com.civicconnect.reporting.feign.*;
import com.civicconnect.reporting.feign.dto.*;
import com.civicconnect.reporting.repository.ReportRepository;
import com.civicconnect.reporting.service.ReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportingService Tests")
class ReportingServiceTest {

    @Mock private ReportRepository          reportRepository;
    @Mock private IdentityFeignClient       identityFeignClient;
    @Mock private ServiceRequestFeignClient serviceRequestFeignClient;
    @Mock private CitizenFeignClient        citizenFeignClient;
    @Mock private ResolutionFeignClient     resolutionFeignClient;
    @Mock private FeedbackFeignClient       feedbackFeignClient;
    @Mock private ComplianceFeignClient     complianceFeignClient;
    @Mock private NotificationFeignClient   notificationFeignClient;

    @InjectMocks
    private ReportingService reportingService;

    private UserValidationResponse cityAdmin;
    private UserValidationResponse serviceOfficer;
    private Report savedReport;

    @BeforeEach
    void setUp() {
        cityAdmin = UserValidationResponse.builder()
                .userId(10L).name("Admin Dave").role("CITY_ADMINISTRATOR")
                .status("ACTIVE").exists(true).build();

        serviceOfficer = UserValidationResponse.builder()
                .userId(20L).name("Officer Bob").role("SERVICE_OFFICER")
                .status("ACTIVE").exists(true).build();

        savedReport = Report.builder()
                .reportId(1L).scope(ReportScope.REQUEST)
                .metrics("SERVICE REQUEST REPORT | Total: 10")
                .generatedByUserId(10L).generatedByName("Admin Dave")
                .generatedDate(LocalDateTime.now()).build();
    }

    // ── generateReport ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should generate REQUEST scope report successfully")
    void shouldGenerateRequestReport() {
        GenerateReportRequest req = new GenerateReportRequest();
        req.setScope(ReportScope.REQUEST);

        ServiceRequestStatsResponse stats = ServiceRequestStatsResponse.builder()
                .total(10).submitted(2).assigned(3).inProgress(2)
                .resolved(1).closed(2).road(5).water(3).electricity(2).build();

        when(identityFeignClient.validateUser(10L)).thenReturn(cityAdmin);
        when(serviceRequestFeignClient.getStats()).thenReturn(stats);
        when(reportRepository.save(any())).thenReturn(savedReport);
        doNothing().when(identityFeignClient).writeAuditLog(any());
        doNothing().when(notificationFeignClient).sendNotification(any());

        ReportResponse response = reportingService.generateReport(req, 10L);

        assertThat(response.getReportId()).isEqualTo(1L);
        assertThat(response.getScope()).isEqualTo(ReportScope.REQUEST);
        assertThat(response.getGeneratedByName()).isEqualTo("Admin Dave");
        verify(reportRepository).save(any(Report.class));
        verify(serviceRequestFeignClient).getStats();
        verify(notificationFeignClient).sendNotification(any());
    }

    @Test
    @DisplayName("Should generate FEEDBACK scope report successfully")
    void shouldGenerateFeedbackReport() {
        GenerateReportRequest req = new GenerateReportRequest();
        req.setScope(ReportScope.FEEDBACK);

        FeedbackStatsResponse stats = FeedbackStatsResponse.builder()
                .totalFeedbacks(25).averageRating(4.2).officersRated(3)
                .topOfficerName("Officer Bob").topOfficerScore(4.8).build();

        Report feedbackReport = Report.builder()
                .reportId(2L).scope(ReportScope.FEEDBACK)
                .metrics("FEEDBACK REPORT | Total Feedbacks: 25")
                .generatedByUserId(10L).generatedByName("Admin Dave")
                .generatedDate(LocalDateTime.now()).build();

        when(identityFeignClient.validateUser(10L)).thenReturn(cityAdmin);
        when(feedbackFeignClient.getStats()).thenReturn(stats);
        when(reportRepository.save(any())).thenReturn(feedbackReport);
        doNothing().when(identityFeignClient).writeAuditLog(any());
        doNothing().when(notificationFeignClient).sendNotification(any());

        ReportResponse response = reportingService.generateReport(req, 10L);

        assertThat(response.getScope()).isEqualTo(ReportScope.FEEDBACK);
        verify(feedbackFeignClient).getStats();
    }

    @Test
    @DisplayName("Should generate COMPLIANCE scope report successfully")
    void shouldGenerateComplianceReport() {
        GenerateReportRequest req = new GenerateReportRequest();
        req.setScope(ReportScope.COMPLIANCE);

        ComplianceStatsResponse stats = ComplianceStatsResponse.builder()
                .totalRecords(20).passCount(15).failCount(5)
                .totalAudits(8).openAudits(3).inReviewAudits(2).closedAudits(3).build();

        Report compReport = Report.builder()
                .reportId(3L).scope(ReportScope.COMPLIANCE)
                .metrics("COMPLIANCE REPORT | Total Compliance Records: 20")
                .generatedByUserId(10L).generatedByName("Admin Dave")
                .generatedDate(LocalDateTime.now()).build();

        when(identityFeignClient.validateUser(10L)).thenReturn(cityAdmin);
        when(complianceFeignClient.getStats()).thenReturn(stats);
        when(reportRepository.save(any())).thenReturn(compReport);
        doNothing().when(identityFeignClient).writeAuditLog(any());
        doNothing().when(notificationFeignClient).sendNotification(any());

        ReportResponse response = reportingService.generateReport(req, 10L);

        assertThat(response.getScope()).isEqualTo(ReportScope.COMPLIANCE);
        verify(complianceFeignClient).getStats();
    }

    @Test
    @DisplayName("Should generate DEPARTMENT scope report — calls all 5 services")
    void shouldGenerateDepartmentReport() {
        GenerateReportRequest req = new GenerateReportRequest();
        req.setScope(ReportScope.DEPARTMENT);

        Report deptReport = Report.builder()
                .reportId(4L).scope(ReportScope.DEPARTMENT)
                .metrics("DEPARTMENT REPORT | Total Citizens: 100")
                .generatedByUserId(10L).generatedByName("Admin Dave")
                .generatedDate(LocalDateTime.now()).build();

        when(identityFeignClient.validateUser(10L)).thenReturn(cityAdmin);
        when(citizenFeignClient.getStats())
                .thenReturn(CitizenStatsResponse.builder().totalCitizens(100).build());
        when(serviceRequestFeignClient.getStats())
                .thenReturn(ServiceRequestStatsResponse.builder()
                        .total(50).closed(30).build());
        when(resolutionFeignClient.getStats())
                .thenReturn(ResolutionStatsResponse.builder()
                        .total(45).completed(40).build());
        when(feedbackFeignClient.getStats())
                .thenReturn(FeedbackStatsResponse.builder()
                        .totalFeedbacks(30).averageRating(4.1).build());
        when(complianceFeignClient.getStats())
                .thenReturn(ComplianceStatsResponse.builder()
                        .totalRecords(20).failCount(2).totalAudits(5).build());
        when(reportRepository.save(any())).thenReturn(deptReport);
        doNothing().when(identityFeignClient).writeAuditLog(any());
        doNothing().when(notificationFeignClient).sendNotification(any());

        ReportResponse response = reportingService.generateReport(req, 10L);

        assertThat(response.getScope()).isEqualTo(ReportScope.DEPARTMENT);
        // Verify all 5 service stats were fetched
        verify(citizenFeignClient).getStats();
        verify(serviceRequestFeignClient).getStats();
        verify(resolutionFeignClient).getStats();
        verify(feedbackFeignClient).getStats();
        verify(complianceFeignClient).getStats();
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when SERVICE_OFFICER tries to generate report")
    void shouldThrowWhenUnauthorisedRole() {
        when(identityFeignClient.validateUser(20L)).thenReturn(serviceOfficer);

        GenerateReportRequest req = new GenerateReportRequest();
        req.setScope(ReportScope.REQUEST);

        assertThatThrownBy(() -> reportingService.generateReport(req, 20L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("CITY_ADMINISTRATOR, DEPARTMENT_HEAD or COMPLIANCE_OFFICER");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowWhenUserNotFound() {
        when(identityFeignClient.validateUser(99L))
                .thenReturn(UserValidationResponse.builder().userId(99L).exists(false).build());

        GenerateReportRequest req = new GenerateReportRequest();
        req.setScope(ReportScope.REQUEST);

        assertThatThrownBy(() -> reportingService.generateReport(req, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ── getReportById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return report by ID")
    void shouldReturnReportById() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(savedReport));

        ReportResponse response = reportingService.getReportById(1L);

        assertThat(response.getReportId()).isEqualTo(1L);
        assertThat(response.getScope()).isEqualTo(ReportScope.REQUEST);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when report not found")
    void shouldThrowWhenReportNotFound() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportingService.getReportById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Report not found");
    }

    // ── getAllReports ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return all reports ordered by date")
    void shouldReturnAllReports() {
        when(reportRepository.findAllByOrderByGeneratedDateDesc())
                .thenReturn(List.of(savedReport));

        List<ReportResponse> results = reportingService.getAllReports();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getGeneratedByName()).isEqualTo("Admin Dave");
    }

    // ── getReportsByScope ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return reports filtered by scope")
    void shouldReturnReportsByScope() {
        when(reportRepository.findByScopeOrderByGeneratedDateDesc(ReportScope.REQUEST))
                .thenReturn(List.of(savedReport));

        List<ReportResponse> results = reportingService.getReportsByScope(ReportScope.REQUEST);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getScope()).isEqualTo(ReportScope.REQUEST);
    }
}
