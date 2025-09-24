package com.obs.controller;

import com.obs.service.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Manager Controller - Branch management and oversight endpoints
 * Enhanced with comprehensive security and role-based access control
 * Accessible by users with MANAGER or ADMIN roles
 */
@RestController
@RequestMapping("/manager")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
@CrossOrigin(origins = "*")
public class ManagerController {

    private final ManagerService managerService;
    
    public ManagerController(ManagerService managerService){ 
        this.managerService = managerService; 
    }

    // Staff Oversight Endpoints
    @GetMapping("/staff/requests") 
    public ResponseEntity<?> getStaffRequests(){ 
        return ResponseEntity.ok(managerService.getStaffRequests()); 
    }
    
    @PostMapping("/staff/requests/{id}/approve") 
    public ResponseEntity<?> approveStaffRequest(@PathVariable String id, @RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(managerService.approveStaffRequest(id, body.getOrDefault("action","APPROVE"))); 
    }
    
    @GetMapping("/staff/performance") 
    public ResponseEntity<?> getStaffPerformance(){ 
        return ResponseEntity.ok(managerService.getStaffPerformance()); 
    }
    
    @PostMapping("/staff/{id}/reset-password") 
    public ResponseEntity<?> resetStaffPassword(@PathVariable String id){ 
        return ResponseEntity.ok(managerService.resetStaffPassword(id)); 
    }

    // Account Management Endpoints
    @GetMapping("/accounts/requests") 
    public ResponseEntity<?> getAccountRequests(){ 
        return ResponseEntity.ok(managerService.getAccountRequests()); 
    }
    
    @PostMapping("/accounts/requests/{id}/process") 
    public ResponseEntity<?> processAccountRequest(@PathVariable String id, @RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(managerService.processAccountRequest(id, body)); 
    }
    
    @GetMapping("/accounts/suspicious") 
    public ResponseEntity<?> getSuspiciousAccounts(){ 
        return ResponseEntity.ok(managerService.getSuspiciousAccounts()); 
    }
    
    @PostMapping("/accounts/{id}/investigate") 
    public ResponseEntity<?> investigateAccount(@PathVariable String id, @RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(managerService.investigateAccount(id, body)); 
    }

    // Transaction Authorization Endpoints
    @GetMapping("/transactions/pending") 
    public ResponseEntity<?> getPendingTransactions(){ 
        return ResponseEntity.ok(managerService.getPendingTransactions()); 
    }
    
    @PostMapping("/transactions/{id}/authorize") 
    public ResponseEntity<?> authorizeTransaction(@PathVariable String id, @RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(managerService.authorizeTransaction(id, body.getOrDefault("action","APPROVE"))); 
    }
    
    @GetMapping("/transactions/chargebacks") 
    public ResponseEntity<?> getChargebacks(){ 
        return ResponseEntity.ok(managerService.getChargebacks()); 
    }
    
    @PostMapping("/transactions/chargebacks/{id}/process") 
    public ResponseEntity<?> processChargeback(@PathVariable String id, @RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(managerService.processChargeback(id, body)); 
    }

    // Compliance & Risk Management Endpoints
    @GetMapping("/compliance/kyc-cases") 
    public ResponseEntity<?> getKycCases(){ 
        return ResponseEntity.ok(managerService.getKycCases()); 
    }
    
    @PostMapping("/compliance/kyc-cases/{id}/process") 
    public ResponseEntity<?> processKycCase(@PathVariable String id, @RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(managerService.processKycCase(id, body)); 
    }
    
    @GetMapping("/compliance/reports") 
    public ResponseEntity<?> getComplianceReports(){ 
        return ResponseEntity.ok(managerService.getComplianceReports()); 
    }
    
    @PostMapping("/compliance/reports/{id}/submit") 
    public ResponseEntity<?> submitComplianceReport(@PathVariable String id){ 
        return ResponseEntity.ok(managerService.submitComplianceReport(id)); 
    }
    
    @GetMapping("/compliance/audit-issues") 
    public ResponseEntity<?> getAuditIssues(){ 
        return ResponseEntity.ok(managerService.getAuditIssues()); 
    }
    
    @PostMapping("/compliance/audit-issues/{id}/resolve") 
    public ResponseEntity<?> resolveAuditIssue(@PathVariable String id, @RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(managerService.resolveAuditIssue(id, body)); 
    }

    // Reporting & Monitoring Endpoints
    @GetMapping("/reports/operational") 
    public ResponseEntity<?> getOperationalReports(){ 
        return ResponseEntity.ok(managerService.getOperationalReports()); 
    }
    
    @PostMapping("/reports/generate") 
    public ResponseEntity<?> generateReport(@RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(managerService.generateReport(body)); 
    }
    
    @GetMapping("/fraud/alerts") 
    public ResponseEntity<?> getFraudAlerts(){ 
        return ResponseEntity.ok(managerService.getFraudAlerts()); 
    }
    
    @PostMapping("/fraud/alerts/{id}/process") 
    public ResponseEntity<?> processFraudAlert(@PathVariable String id, @RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(managerService.processFraudAlert(id, body.getOrDefault("action","INVESTIGATE"))); 
    }

    // System Access & Security Endpoints
    @GetMapping("/system-access") 
    public ResponseEntity<?> getSystemAccess(){ 
        return ResponseEntity.ok(managerService.getSystemAccess()); 
    }
    
    @PostMapping("/system-access/{id}/manage") 
    public ResponseEntity<?> manageSystemAccess(@PathVariable String id, @RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(managerService.manageSystemAccess(id, body.getOrDefault("action","ENABLE"))); 
    }
    
    @GetMapping("/team-access") 
    public ResponseEntity<?> getTeamAccess(){ 
        return ResponseEntity.ok(managerService.getTeamAccess()); 
    }
    
    @PostMapping("/team-access/{id}/manage") 
    public ResponseEntity<?> manageEmployeeAccess(@PathVariable String id, @RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(managerService.manageEmployeeAccess(id, body.getOrDefault("action","UNLOCK"))); 
    }
    
    @GetMapping("/security/logs") 
    public ResponseEntity<?> getSecurityLogs(){ 
        return ResponseEntity.ok(managerService.getSecurityLogs()); 
    }

    // Customer Issue Resolution Endpoints
    @GetMapping("/complaints") 
    public ResponseEntity<?> getComplaints(){ 
        return ResponseEntity.ok(managerService.getComplaints()); 
    }
    
    @PostMapping("/complaints/{id}/process") 
    public ResponseEntity<?> processComplaint(@PathVariable String id, @RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(managerService.processComplaint(id, body)); 
    }
    
    @GetMapping("/disputes") 
    public ResponseEntity<?> getDisputes(){ 
        return ResponseEntity.ok(managerService.getDisputes()); 
    }
    
    @PostMapping("/disputes/{id}/process") 
    public ResponseEntity<?> processDispute(@PathVariable String id, @RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(managerService.processDispute(id, body)); 
    }
    
    @GetMapping("/escalations") 
    public ResponseEntity<?> getEscalations(){ 
        return ResponseEntity.ok(managerService.getEscalations()); 
    }
    
    @PostMapping("/escalations/{id}/process") 
    public ResponseEntity<?> processEscalation(@PathVariable String id, @RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(managerService.processEscalation(id, body)); 
    }

    // Legacy endpoints for backward compatibility
    @GetMapping("/loans/pending") 
    public ResponseEntity<?> pendingLoans(){ 
        return ResponseEntity.ok(managerService.getPendingLoans()); 
    }
    
    @PostMapping("/loans/{id}/approve") 
    public ResponseEntity<?> approve(@PathVariable("id") String id, @RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(managerService.approveLoan(id, body.getOrDefault("decision","PENDING"))); 
    }
    
    @GetMapping("/reports/branch") 
    public ResponseEntity<?> branchReports(){ 
        return ResponseEntity.ok(managerService.branchReports()); 
    }
    
    @GetMapping("/employees/performance") 
    public ResponseEntity<?> employeePerf(){ 
        return ResponseEntity.ok(managerService.employeePerformance()); 
    }
}
