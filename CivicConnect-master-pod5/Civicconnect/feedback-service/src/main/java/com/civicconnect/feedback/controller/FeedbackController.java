package com.civicconnect.feedback.controller;

import com.civicconnect.feedback.dto.request.SubmitFeedbackRequest;
import com.civicconnect.feedback.dto.response.FeedbackResponse;
import com.civicconnect.feedback.dto.response.SatisfactionMetricResponse;
import com.civicconnect.feedback.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback & Satisfaction",
     description = "Citizen feedback submission and officer satisfaction metrics")
@SecurityRequirement(name = "BearerAuth")
public class FeedbackController {

    private final FeedbackService feedbackService;

    // ── POST /api/v1/feedback — CITIZEN only ──────────────────────────────────
    @Operation(
        summary = "Submit feedback — CITIZEN only",
        description = "Submit a rating (1-5) and optional comments for a CLOSED service request. "
                    + "citizenId is resolved from the JWT. One feedback per request maximum."
    )
    @ApiResponse(responseCode = "201", description = "Feedback submitted")
    @ApiResponse(responseCode = "400", description = "Request not CLOSED, not your request, or already submitted")
    @ApiResponse(responseCode = "404", description = "Citizen or service request not found")
    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @Valid @RequestBody SubmitFeedbackRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.submitFeedback(extractUserId(authentication), request));
    }

    // ── GET /api/v1/feedback/request/{requestId} — CITIZEN | officers ─────────
    @Operation(summary = "Get feedback by service request ID")
    @ApiResponse(responseCode = "200", description = "Feedback found")
    @ApiResponse(responseCode = "404", description = "No feedback for this request")
    @PreAuthorize("hasAnyRole('CITIZEN','SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @GetMapping("/request/{requestId}")
    public ResponseEntity<FeedbackResponse> getFeedbackByRequestId(
            @PathVariable Long requestId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByRequestId(requestId));
    }

    // ── GET /api/v1/feedback/citizen/{citizenId} — CITIZEN | officers ─────────
    @Operation(summary = "Get all feedback submitted by a citizen")
    @ApiResponse(responseCode = "200", description = "List returned")
    @PreAuthorize("hasAnyRole('CITIZEN','SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksByCitizenId(
            @PathVariable Long citizenId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByCitizenId(citizenId));
    }

    // ── GET /api/v1/feedback/metrics/officer/{officerId} — SERVICE_OFFICER+ ───
    @Operation(summary = "Get satisfaction metric for one officer — SERVICE_OFFICER and above")
    @ApiResponse(responseCode = "200", description = "Metric found")
    @ApiResponse(responseCode = "404", description = "No metric for this officer yet")
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @GetMapping("/metrics/officer/{officerId}")
    public ResponseEntity<SatisfactionMetricResponse> getSatisfactionMetricByOfficerId(
            @PathVariable Long officerId) {
        return ResponseEntity.ok(feedbackService.getSatisfactionMetricByOfficerId(officerId));
    }

    // ── GET /api/v1/feedback/metrics — DEPT_HEAD | ADMIN | COMPLIANCE ────────
    @Operation(
        summary = "Get all satisfaction metrics (leaderboard) — DEPARTMENT_HEAD, CITY_ADMINISTRATOR, COMPLIANCE_OFFICER",
        description = "Returns all officers ordered by average satisfaction score descending."
    )
    @ApiResponse(responseCode = "200", description = "Leaderboard returned")
    @PreAuthorize("hasAnyRole('DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping("/metrics")
    public ResponseEntity<List<SatisfactionMetricResponse>> getAllSatisfactionMetrics() {
        return ResponseEntity.ok(feedbackService.getAllSatisfactionMetrics());
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Long extractUserId(Authentication auth) {
        return ((Number) ((UsernamePasswordAuthenticationToken) auth).getDetails()).longValue();
    }
}
