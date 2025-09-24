package com.obs.controller;

import com.obs.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin Controller - Full system administration endpoints
 * Enhanced with comprehensive security and role-based access control
 * Accessible only by users with ADMIN role
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) { 
        this.adminService = adminService; 
    }

    // Dashboard
    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> dashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // User Management Endpoints
    @GetMapping("/users") 
    public ResponseEntity<?> users() { 
        return ResponseEntity.ok(adminService.getAllUsers()); 
    }
    
    @PostMapping("/users") 
    public ResponseEntity<?> createUser(@RequestBody Map<String,Object> data){ 
        return ResponseEntity.ok(adminService.createUser(data)); 
    }
    
    @PutMapping("/users/{id}") 
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody Map<String,Object> data){ 
        return ResponseEntity.ok(adminService.updateUser(id,data)); 
    }
    
    @DeleteMapping("/users/{id}") 
    public ResponseEntity<?> deleteUser(@PathVariable String id){ 
        adminService.deleteUser(id); 
        return ResponseEntity.noContent().build(); 
    }
    
    @PostMapping("/users/{id}/unlock") 
    public ResponseEntity<?> unlock(@PathVariable String id){ 
        return ResponseEntity.ok(adminService.unlockUser(id)); 
    }

    @PostMapping("/users/{id}/lock") 
    public ResponseEntity<?> lock(@PathVariable String id){ 
        return ResponseEntity.ok(adminService.lockUser(id)); 
    }

    @PostMapping("/users/{id}/reset-password") 
    public ResponseEntity<?> resetPassword(@PathVariable String id){ 
        return ResponseEntity.ok(adminService.resetUserPassword(id)); 
    }
    
    @GetMapping("/users/{id}/activity") 
    public ResponseEntity<?> getUserActivity(@PathVariable String id){ 
        return ResponseEntity.ok(adminService.getUserActivity(id)); 
    }
    
    @PostMapping("/users/bulk-actions") 
    public ResponseEntity<?> bulkUserActions(@RequestBody Map<String,Object> data){ 
        return ResponseEntity.ok(adminService.performBulkUserActions(data)); 
    }

    // Account Management Endpoints
    @GetMapping("/accounts")
    public ResponseEntity<?> getAllAccounts(){
        return ResponseEntity.ok(adminService.getAllAccounts());
    }
    
    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.createAccount(data));
    }
    
    @PutMapping("/accounts/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable String id, @RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.updateAccount(id, data));
    }
    
    @PostMapping("/accounts/{id}/freeze")
    public ResponseEntity<?> freezeAccount(@PathVariable String id, @RequestBody Map<String,String> data){
        return ResponseEntity.ok(adminService.freezeAccount(id, data));
    }
    
    @PostMapping("/accounts/{id}/unfreeze")
    public ResponseEntity<?> unfreezeAccount(@PathVariable String id){
        return ResponseEntity.ok(adminService.unfreezeAccount(id));
    }
    
    @PostMapping("/accounts/{id}/close")
    public ResponseEntity<?> closeAccount(@PathVariable String id, @RequestBody Map<String,String> data){
        return ResponseEntity.ok(adminService.closeAccount(id, data));
    }
    
    @GetMapping("/accounts/{id}/history")
    public ResponseEntity<?> getAccountHistory(@PathVariable String id){
        return ResponseEntity.ok(adminService.getAccountHistory(id));
    }

    // Transaction Monitoring Endpoints
    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions(){
        return ResponseEntity.ok(adminService.getAllTransactions());
    }
    
    @GetMapping("/transactions/suspicious")
    public ResponseEntity<?> getSuspiciousTransactions(){
        return ResponseEntity.ok(adminService.getSuspiciousTransactions());
    }
    
    @PostMapping("/transactions/{id}/flag")
    public ResponseEntity<?> flagTransaction(@PathVariable String id, @RequestBody Map<String,String> data){
        return ResponseEntity.ok(adminService.flagTransaction(id, data));
    }
    
    @PostMapping("/transactions/{id}/reverse")
    public ResponseEntity<?> reverseTransaction(@PathVariable String id, @RequestBody Map<String,String> data){
        return ResponseEntity.ok(adminService.reverseTransaction(id, data));
    }
    
    @GetMapping("/transactions/patterns")
    public ResponseEntity<?> getTransactionPatterns(){
        return ResponseEntity.ok(adminService.getTransactionPatterns());
    }
    
    @PostMapping("/transactions/limits")
    public ResponseEntity<?> updateTransactionLimits(@RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.updateTransactionLimits(data));
    }

    // Security & Compliance Endpoints
    @GetMapping("/security/policies")
    public ResponseEntity<?> getSecurityPolicies(){
        return ResponseEntity.ok(adminService.getSecurityPolicies());
    }
    
    @PostMapping("/security/policies")
    public ResponseEntity<?> updateSecurityPolicy(@RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.updateSecurityPolicy(data));
    }
    
    @GetMapping("/compliance/status")
    public ResponseEntity<?> getComplianceStatus(){
        return ResponseEntity.ok(adminService.getComplianceStatus());
    }
    
    @PostMapping("/compliance/scan")
    public ResponseEntity<?> triggerComplianceScan(){
        return ResponseEntity.ok(adminService.triggerComplianceScan());
    }
    
    @GetMapping("/security/incidents")
    public ResponseEntity<?> getSecurityIncidents(){
        return ResponseEntity.ok(adminService.getSecurityIncidents());
    }
    
    @PostMapping("/security/incidents/{id}/resolve")
    public ResponseEntity<?> resolveSecurityIncident(@PathVariable String id, @RequestBody Map<String,String> data){
        return ResponseEntity.ok(adminService.resolveSecurityIncident(id, data));
    }

    // System Configuration Endpoints
    @GetMapping("/config/system")
    public ResponseEntity<?> getSystemConfig(){
        return ResponseEntity.ok(adminService.getSystemConfiguration());
    }
    
    @PostMapping("/config/system")
    public ResponseEntity<?> updateSystemConfig(@RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.updateSystemConfiguration(data));
    }
    
    @GetMapping("/config/features")
    public ResponseEntity<?> getFeatureFlags(){
        return ResponseEntity.ok(adminService.getFeatureFlags());
    }
    
    @PostMapping("/config/features")
    public ResponseEntity<?> updateFeatureFlags(@RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.updateFeatureFlags(data));
    }
    
    @PostMapping("/system/maintenance")
    public ResponseEntity<?> scheduleMaintenanceMode(@RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.scheduleMaintenanceMode(data));
    }
    
    @GetMapping("/integrations")
    public ResponseEntity<?> getSystemIntegrations(){
        return ResponseEntity.ok(adminService.getSystemIntegrations());
    }
    
    @PostMapping("/integrations/{id}/test")
    public ResponseEntity<?> testIntegration(@PathVariable String id){
        return ResponseEntity.ok(adminService.testIntegration(id));
    }

    // Reporting & Analytics Endpoints
    @GetMapping("/reports/comprehensive")
    public ResponseEntity<?> getComprehensiveReports(){
        return ResponseEntity.ok(adminService.getComprehensiveReports());
    }
    
    @PostMapping("/reports/generate")
    public ResponseEntity<?> generateCustomReport(@RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.generateCustomReport(data));
    }
    
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<?> getAnalyticsDashboard(){
        return ResponseEntity.ok(adminService.getAnalyticsDashboard());
    }
    
    @GetMapping("/analytics/trends")
    public ResponseEntity<?> getBusinessTrends(){
        return ResponseEntity.ok(adminService.getBusinessTrends());
    }
    
    @PostMapping("/reports/schedule")
    public ResponseEntity<?> scheduleReport(@RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.scheduleReport(data));
    }

    // Disaster Recovery Endpoints
    @GetMapping("/backup/status")
    public ResponseEntity<?> getBackupStatus(){
        return ResponseEntity.ok(adminService.getBackupStatus());
    }
    
    @PostMapping("/backup/create")
    public ResponseEntity<?> createBackup(@RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.createBackup(data));
    }
    
    @PostMapping("/restore/initiate")
    public ResponseEntity<?> initiateRestore(@RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.initiateRestore(data));
    }
    
    @GetMapping("/disaster-recovery/plan")
    public ResponseEntity<?> getDisasterRecoveryPlan(){
        return ResponseEntity.ok(adminService.getDisasterRecoveryPlan());
    }
    
    @PostMapping("/disaster-recovery/test")
    public ResponseEntity<?> testDisasterRecovery(){
        return ResponseEntity.ok(adminService.testDisasterRecovery());
    }

    // Audit & Logs Endpoints
    @GetMapping("/audit/trails")
    public ResponseEntity<?> getAuditTrails(){
        return ResponseEntity.ok(adminService.getAuditTrails());
    }
    
    @GetMapping("/audit/trails/{userId}")
    public ResponseEntity<?> getUserAuditTrail(@PathVariable String userId){
        return ResponseEntity.ok(adminService.getUserAuditTrail(userId));
    }
    
    @PostMapping("/audit/export")
    public ResponseEntity<?> exportAuditLogs(@RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.exportAuditLogs(data));
    }
    
    @GetMapping("/logs/system")
    public ResponseEntity<?> getSystemLogs(){
        return ResponseEntity.ok(adminService.getSystemLogs());
    }
    
    @GetMapping("/logs/security")
    public ResponseEntity<?> getSecurityLogs(){
        return ResponseEntity.ok(adminService.getSecurityLogs());
    }
    
    @PostMapping("/audit/compliance-report")
    public ResponseEntity<?> generateComplianceAuditReport(@RequestBody Map<String,Object> data){
        return ResponseEntity.ok(adminService.generateComplianceAuditReport(data));
    }

    // Branches (existing functionality)
    @GetMapping("/branches") 
    public ResponseEntity<?> branches(){ 
        return ResponseEntity.ok(adminService.getAllBranches()); 
    }
    
    @PostMapping("/branches") 
    public ResponseEntity<?> createBranch(@RequestBody Map<String,Object> data){ 
        return ResponseEntity.ok(adminService.createBranch(data)); 
    }
    
    @PutMapping("/branches/{id}") 
    public ResponseEntity<?> updateBranch(@PathVariable String id, @RequestBody Map<String,Object> data){ 
        return ResponseEntity.ok(adminService.updateBranch(id,data)); 
    }
    
    @DeleteMapping("/branches/{id}") 
    public ResponseEntity<?> deleteBranch(@PathVariable String id){ 
        adminService.deleteBranch(id); 
        return ResponseEntity.noContent().build(); 
    }

    // Branch Audits (existing functionality)
    @GetMapping("/branches/audits") 
    public ResponseEntity<?> recentAudits(){ 
        return ResponseEntity.ok(adminService.getRecentBranchAudits()); 
    }
    
    @GetMapping("/branches/{id}/audits") 
    public ResponseEntity<?> audits(@PathVariable String id){ 
        return ResponseEntity.ok(adminService.getBranchAudits(id)); 
    }

    // Legacy endpoints for backward compatibility
    @GetMapping("/security/reports") 
    public ResponseEntity<?> securityReports(){ 
        return ResponseEntity.ok(adminService.getSecurityReports()); 
    }

    @GetMapping("/reports") 
    public ResponseEntity<?> reports(){ 
        return ResponseEntity.ok(adminService.getSystemReports()); 
    }
    
    @GetMapping("/security-logs") 
    public ResponseEntity<?> logs(){ 
        return ResponseEntity.ok(adminService.getSecurityLogs()); 
    }
}
