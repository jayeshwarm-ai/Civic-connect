package com.civicconnect.reporting.service.impl;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {

    private final ReportRepository          reportRepository;
    private final IdentityFeignClient       identityFeignClient;
    private final ServiceRequestFeignClient serviceRequestFeignClient;
    private final CitizenFeignClient        citizenFeignClient;
    private final ResolutionFeignClient     resolutionFeignClient;
    private final FeedbackFeignClient       feedbackFeignClient;
    private final ComplianceFeignClient     complianceFeignClient;
    private final NotificationFeignClient   notificationFeignClient;

    @Override
    @Transactional
    public ReportResponse generateReport(GenerateReportRequest request, Long userId) {
        UserValidationResponse user = identityFeignClient.validateUser(userId);
        if (!user.isExists()) throw new ResourceNotFoundException("User", userId);

        String role = user.getRole();
        if (!"CITY_ADMINISTRATOR".equals(role) && !"DEPARTMENT_HEAD".equals(role) && !"COMPLIANCE_OFFICER".equals(role))
            throw new InvalidOperationException("Only CITY_ADMINISTRATOR, DEPARTMENT_HEAD or COMPLIANCE_OFFICER can generate reports.");

        String metrics = switch (request.getScope()) {
            case REQUEST -> buildRequestMetrics();
            case FEEDBACK -> buildFeedbackMetrics();
            case COMPLIANCE -> buildComplianceMetrics();
            case DEPARTMENT -> buildDepartmentMetrics();
        };

        Report report = Report.builder()
                .scope(request.getScope()).metrics(metrics)
                .generatedByUserId(userId).generatedByName(user.getName()).build();
        report = reportRepository.save(report);

        writeAuditLog(userId, "REPORT_GENERATED", "REPORT",
                String.valueOf(report.getReportId()), "Report generated with scope: " + request.getScope());
        sendNotification(userId, "Report generated successfully. Scope: " + request.getScope() + ". Report ID: " + report.getReportId());
        return mapToResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long reportId) {
        return mapToResponse(reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", reportId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAllByOrderByGeneratedDateDesc()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByScope(ReportScope scope) {
        return reportRepository.findByScopeOrderByGeneratedDateDesc(scope)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private String buildRequestMetrics() {
        ServiceRequestStatsResponse s = serviceRequestFeignClient.getStats();
        return String.format("SERVICE REQUEST REPORT | Total: %d | Submitted: %d | Assigned: %d | In Progress: %d | Resolved: %d | Closed: %d | Road: %d | Water: %d | Electricity: %d",
                s.getTotal(), s.getSubmitted(), s.getAssigned(), s.getInProgress(), s.getResolved(), s.getClosed(), s.getRoad(), s.getWater(), s.getElectricity());
    }

    private String buildFeedbackMetrics() {
        FeedbackStatsResponse f = feedbackFeignClient.getStats();
        String topOfficer = f.getOfficersRated() == 0 ? "N/A" : f.getTopOfficerName() + " (Score: " + String.format("%.2f", f.getTopOfficerScore()) + ")";
        return String.format("FEEDBACK REPORT | Total Feedbacks: %d | Overall Average Rating: %.2f/5 | Officers Rated: %d | Top Performing Officer: %s",
                f.getTotalFeedbacks(), f.getAverageRating(), f.getOfficersRated(), topOfficer);
    }

    private String buildComplianceMetrics() {
        ComplianceStatsResponse c = complianceFeignClient.getStats();
        double passRate = c.getTotalRecords() > 0 ? ((double) c.getPassCount() / c.getTotalRecords()) * 100 : 0.0;
        return String.format("COMPLIANCE REPORT | Total Compliance Records: %d | Pass: %d | Fail: %d | Pass Rate: %.1f%% | Total Audits: %d | Open: %d | In Review: %d | Closed: %d",
                c.getTotalRecords(), c.getPassCount(), c.getFailCount(), passRate, c.getTotalAudits(), c.getOpenAudits(), c.getInReviewAudits(), c.getClosedAudits());
    }

    private String buildDepartmentMetrics() {
        CitizenStatsResponse ci = citizenFeignClient.getStats();
        ServiceRequestStatsResponse sr = serviceRequestFeignClient.getStats();
        ResolutionStatsResponse rs = resolutionFeignClient.getStats();
        FeedbackStatsResponse fb = feedbackFeignClient.getStats();
        ComplianceStatsResponse co = complianceFeignClient.getStats();
        double resolutionRate = sr.getTotal() > 0 ? ((double) sr.getClosed() / sr.getTotal()) * 100 : 0.0;
        return String.format("DEPARTMENT REPORT | Total Citizens: %d | Total Requests: %d | Closed: %d | Resolution Rate: %.1f%% | Total Resolutions: %d | Completed: %d | Total Feedbacks: %d | Avg Rating: %.2f/5 | Compliance Records: %d | Failed: %d | Total Audits: %d",
                ci.getTotalCitizens(), sr.getTotal(), sr.getClosed(), resolutionRate, rs.getTotal(), rs.getCompleted(), fb.getTotalFeedbacks(), fb.getAverageRating(), co.getTotalRecords(), co.getFailCount(), co.getTotalAudits());
    }

    private void writeAuditLog(Long performedBy, String action, String resource, String resourceId, String detail) {
        try {
            identityFeignClient.writeAuditLog(AuditLogRequest.builder()
                    .performedBy(performedBy).action(action).resource(resource).resourceId(resourceId).detail(detail).build());
        } catch (Exception e) { log.warn("Failed to write audit log: action={}: {}", action, e.getMessage()); }
    }

    private void sendNotification(Long userId, String message) {
        try {
            notificationFeignClient.sendNotification(SendNotificationRequest.builder().userId(userId).message(message).category("REPORT").build());
        } catch (Exception e) { log.warn("Failed to send notification to userId={}: {}", userId, e.getMessage()); }
    }

    private ReportResponse mapToResponse(Report r) {
        return ReportResponse.builder()
                .reportId(r.getReportId()).scope(r.getScope()).metrics(r.getMetrics())
                .generatedByUserId(r.getGeneratedByUserId()).generatedByName(r.getGeneratedByName())
                .generatedDate(r.getGeneratedDate()).build();
    }
}

