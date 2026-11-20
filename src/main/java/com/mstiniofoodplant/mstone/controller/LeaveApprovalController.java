package com.mstiniofoodplant.mstone.controller;

import com.mstiniofoodplant.mstone.dto.ApiResponse;
import com.mstiniofoodplant.mstone.dto.leave.ApprovalDecisionRequest;
import com.mstiniofoodplant.mstone.exception.InsufficientLeaveBalanceException;
import com.mstiniofoodplant.mstone.exception.InvalidLeaveRequestException;
import com.mstiniofoodplant.mstone.model.Employees;
import com.mstiniofoodplant.mstone.security.CurrentUserResolver;
import com.mstiniofoodplant.mstone.service.LeaveService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/leave")
public class LeaveApprovalController {

    private final LeaveService leaveService;
    private final CurrentUserResolver currentUserResolver;

    public LeaveApprovalController(LeaveService leaveService, CurrentUserResolver currentUserResolver) {
        this.leaveService = leaveService;
        this.currentUserResolver = currentUserResolver;
    }

    /**
     * GET /api/leave/approvals/pending - View pending approvals
     * Restricted: Software Engineer has full access; Employee is forbidden.
     */
    @GetMapping("/approvals/pending")
    public ResponseEntity<ApiResponse> getPendingApprovals(
            HttpServletRequest request,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) UUID approverId) {
        try {
            currentUserResolver.requirePrivilegedAccess(request, "Viewing pending approvals");
            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Pending approvals retrieved successfully",
                    leaveService.getPendingApprovals(stage, approverId)));
        } catch (AccessDeniedException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, exception.getMessage()));
        }
    }

    /**
     * POST /api/leave/approvals/{id}/approve - Approve
     * Restricted: Software Engineer has full access; Employee is forbidden.
     */
    @PostMapping("/approvals/{id}/approve")
    public ResponseEntity<ApiResponse> approveApplication(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestBody(required = false) ApprovalDecisionRequest decisionRequest) {
        try {
            currentUserResolver.requirePrivilegedAccess(request, "Approving leave applications");
            Employees approver = currentUserResolver.resolveCurrentEmployee(request);
            String remarks = decisionRequest != null ? decisionRequest.getRemarks() : null;
            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Leave application approved successfully",
                    leaveService.approveApplication(id, approver, remarks)));
        } catch (AccessDeniedException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, exception.getMessage()));
        } catch (InsufficientLeaveBalanceException | InvalidLeaveRequestException | IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, exception.getMessage()));
        }
    }

    /**
     * POST /api/leave/approvals/{id}/reject - Reject
     * Restricted: Software Engineer has full access; Employee is forbidden.
     */
    @PostMapping("/approvals/{id}/reject")
    public ResponseEntity<ApiResponse> rejectApplication(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestBody(required = false) ApprovalDecisionRequest decisionRequest) {
        try {
            currentUserResolver.requirePrivilegedAccess(request, "Rejecting leave applications");
            Employees approver = currentUserResolver.resolveCurrentEmployee(request);
            String remarks = decisionRequest != null ? decisionRequest.getRemarks() : null;
            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Leave application rejected",
                    leaveService.rejectApplication(id, approver, remarks)));
        } catch (AccessDeniedException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, exception.getMessage()));
        } catch (InsufficientLeaveBalanceException | InvalidLeaveRequestException | IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, exception.getMessage()));
        }
    }
}
