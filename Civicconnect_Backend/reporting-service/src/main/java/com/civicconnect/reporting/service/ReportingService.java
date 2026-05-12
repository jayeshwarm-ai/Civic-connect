package com.civicconnect.reporting.service;

import com.civicconnect.reporting.dto.request.GenerateReportRequest;
import com.civicconnect.reporting.dto.response.ReportResponse;
import com.civicconnect.reporting.enums.ReportScope;

import java.util.List;

/**
 * Service interface for Reporting operations.
 */
public interface ReportingService {
    ReportResponse generateReport(GenerateReportRequest request, Long userId);
    ReportResponse getReportById(Long reportId);
    List<ReportResponse> getAllReports();
    List<ReportResponse> getReportsByScope(ReportScope scope);
}
