package com.mstiniofoodplant.mstone.controller;

import com.mstiniofoodplant.mstone.dto.ApiResponse;
import com.mstiniofoodplant.mstone.dto.leave.LeaveCancellationRequest;
import com.mstiniofoodplant.mstone.dto.leave.LeaveCreateRequest;
import com.mstiniofoodplant.mstone.dto.leave.LeaveRequestDetailResponse;
import com.mstiniofoodplant.mstone.exception.InsufficientLeaveBalanceException;
import com.mstiniofoodplant.mstone.exception.InvalidLeaveRequestException;
import com.mstiniofoodplant.mstone.model.Employees;
import com.mstiniofoodplant.mstone.security.CurrentUserResolver;
import com.mstiniofoodplant.mstone.service.LeaveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/leave")
public class LeaveRequestController {

    private final LeaveService leaveService;
    private final CurrentUserResolver currentUserResolver;

    public LeaveRequestController(LeaveService leaveService, CurrentUserResolver currentUserResolver) {
        this.leaveService = leaveService;
        this.currentUserResolver = currentUserResolver;
    }

    /**
     * GET /api/leave/types - List available leave types
     */
    @GetMapping("/types")
    public ResponseEntity<ApiResponse> getLeaveTypes() {
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Leave types retrieved successfully",
                leaveService.getAvailableLeaveTypes()));
    }

    /**
     * GET /api/leave/requests/me - View my applications
     */
    @GetMapping("/requests/me")
    public ResponseEntity<ApiResponse> getMyRequests(HttpServletRequest request) {
        try {
            Employees employee = currentUserResolver.resolveCurrentEmployee(request);
            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Leave applications retrieved successfully",
                    leaveService.getMyRequests(employee)));
        } catch (InsufficientLeaveBalanceException | InvalidLeaveRequestException | IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, exception.getMessage()));
        }
    }

    /**
     * POST /api/leave/requests - Create application (Draft)
     * Fails if leave balance is used up or insufficient.
     */
    @PostMapping("/requests")
    public ResponseEntity<ApiResponse> createApplication(
            HttpServletRequest request,
            @Valid @RequestBody LeaveCreateRequest createRequest) {
        try {
            Employees employee = currentUserResolver.resolveCurrentEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(
                    true,
                    "Leave application created successfully as draft",
                    leaveService.createApplication(employee, createRequest)));
        } catch (InsufficientLeaveBalanceException | InvalidLeaveRequestException | IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, exception.getMessage()));
        }
    }

    /**
     * POST /api/leave/requests/{id}/submit - Submit application
     * Fails if leave balance is used up or insufficient.
     */
    @PostMapping("/requests/{id}/submit")
    public ResponseEntity<ApiResponse> submitApplication(
            HttpServletRequest request,
            @PathVariable UUID id) {
        try {
            Employees employee = currentUserResolver.resolveCurrentEmployee(request);
            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Leave application submitted successfully for review",
                    leaveService.submitApplication(id, employee)));
        } catch (InsufficientLeaveBalanceException | InvalidLeaveRequestException | IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, exception.getMessage()));
        }
    }

    /**
     * POST /api/leave/requests/{id}/cancel - Request cancellation
     */
    @PostMapping("/requests/{id}/cancel")
    public ResponseEntity<ApiResponse> cancelApplication(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestBody(required = false) LeaveCancellationRequest cancellationRequest) {
        try {
            Employees employee = currentUserResolver.resolveCurrentEmployee(request);
            String reason = cancellationRequest != null ? cancellationRequest.getReason() : null;
            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Leave application cancelled successfully",
                    leaveService.cancelApplication(id, employee, reason)));
        } catch (InsufficientLeaveBalanceException | InvalidLeaveRequestException | IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, exception.getMessage()));
        }
    }

    /**
     * GET /api/leave/requests/{id} - View application details
     * Software Engineer can view any; Employee can only view their own request.
     */
    @GetMapping("/requests/{id}")
    public ResponseEntity<ApiResponse> getRequestDetails(
            HttpServletRequest request,
            @PathVariable UUID id) {
        try {
            LeaveRequestDetailResponse details = leaveService.getRequestDetails(id);
            if (!currentUserResolver.isSuperUser(request)) {
                Employees currentEmployee = currentUserResolver.resolveCurrentEmployee(request);
                if (details.getRequest() == null || !currentEmployee.getId().equals(details.getRequest().getEmployeeId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new ApiResponse(false, "Access denied: You can only view your own leave applications."));
                }
            }
            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Leave application details retrieved successfully",
                    details));
        } catch (InvalidLeaveRequestException | IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, exception.getMessage()));
        }
    }

    /**
     * GET /api/leave/calendar - View approved leave calendar
     */
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse> getApprovedCalendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID departmentId) {
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Approved leave calendar retrieved successfully",
                leaveService.getApprovedCalendar(startDate, endDate, departmentId)));
    }
}
