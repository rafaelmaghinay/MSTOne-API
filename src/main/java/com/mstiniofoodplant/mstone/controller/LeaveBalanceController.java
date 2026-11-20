package com.mstiniofoodplant.mstone.controller;

import com.mstiniofoodplant.mstone.dto.ApiResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/leave")
public class LeaveBalanceController {

    private final LeaveService leaveService;
    private final CurrentUserResolver currentUserResolver;

    public LeaveBalanceController(LeaveService leaveService, CurrentUserResolver currentUserResolver) {
        this.leaveService = leaveService;
        this.currentUserResolver = currentUserResolver;
    }

    /**
     * GET /api/leave/balances/me - View my leave balances
     * Allowed for both Employee and Software Engineer.
     */
    @GetMapping("/balances/me")
    public ResponseEntity<ApiResponse> getMyBalances(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year) {
        try {
            Employees employee = currentUserResolver.resolveCurrentEmployee(request);
            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Leave balances retrieved successfully",
                    leaveService.getMyBalances(employee, year)));
        } catch (InsufficientLeaveBalanceException | InvalidLeaveRequestException | IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, exception.getMessage()));
        }
    }

    /**
     * GET /api/leave/admin/balances - HR balance management
     * Restricted: Software Engineer has full access; Employee is forbidden.
     */
    @GetMapping("/admin/balances")
    public ResponseEntity<ApiResponse> getAdminBalances(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID leaveTypeId) {
        try {
            currentUserResolver.requirePrivilegedAccess(request, "HR balance overview");
            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Leave balances retrieved successfully",
                    leaveService.getAdminBalances(year, employeeId, leaveTypeId)));
        } catch (AccessDeniedException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, exception.getMessage()));
        } catch (InsufficientLeaveBalanceException | InvalidLeaveRequestException | IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, exception.getMessage()));
        }
    }
}
