package com.obs.service;

import com.obs.model.LoanApplication;
import com.obs.model.LoanStatus;
import com.obs.repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.time.LocalDateTime;

@Service
public class ManagerService {
    private final LoanApplicationRepository loanRepository;
    
    public ManagerService(LoanApplicationRepository loanRepository){ 
        this.loanRepository = loanRepository; 
    }

    // Staff Oversight Methods
    public List<Map<String,Object>> getStaffRequests(){
        List<Map<String,Object>> requests = new ArrayList<>();
        requests.add(Map.of(
            "id", "REQ001",
            "employeeName", "Suresh Patel",
            "requestType", "ACCOUNT_OPENING",
            "status", "PENDING",
            "submittedDate", LocalDateTime.now().minusHours(2).toString(),
            "priority", "MEDIUM"
        ));
        requests.add(Map.of(
            "id", "REQ002",
            "employeeName", "Kavita Reddy",
            "requestType", "ACCESS_LEVEL_INCREASE",
            "status", "PENDING",
            "submittedDate", LocalDateTime.now().minusHours(4).toString(),
            "priority", "HIGH"
        ));
        return requests;
    }
    
    public Map<String,Object> approveStaffRequest(String id, String action){
        return Map.of(
            "success", true,
            "message", "Staff request " + action.toLowerCase() + "ed successfully",
            "requestId", id,
            "action", action
        );
    }
    
    public List<Map<String,Object>> getStaffPerformance(){
        List<Map<String,Object>> performance = new ArrayList<>();
        performance.add(Map.of(
            "employeeId", "EMP001",
            "employeeName", "Suresh Patel",
            "performanceScore", 92,
            "transactionsProcessed", 567,
            "customerRating", 4.7,
            "complianceScore", 96
        ));
        performance.add(Map.of(
            "employeeId", "EMP002",
            "employeeName", "Kavita Reddy",
            "performanceScore", 88,
            "transactionsProcessed", 245,
            "customerRating", 4.5,
            "complianceScore", 94
        ));
        return performance;
    }
    
    public Map<String,Object> resetStaffPassword(String id){
        return Map.of(
            "success", true,
            "message", "Password reset successfully for employee: " + id,
            "employeeId", id
        );
    }

    // Account Management Methods
    public List<Map<String,Object>> getAccountRequests(){
        List<Map<String,Object>> requests = new ArrayList<>();
        requests.add(Map.of(
            "id", "ACC001",
            "customerName", "Rajesh Kumar",
            "requestType", "ACCOUNT_OPENING",
            "accountType", "SAVINGS",
            "status", "PENDING_MANAGER_APPROVAL",
            "submittedDate", LocalDateTime.now().minusHours(1).toString(),
            "amount", 50000
        ));
        return requests;
    }
    
    public Map<String,Object> processAccountRequest(String id, Map<String,Object> data){
        return Map.of(
            "success", true,
            "message", "Account request processed successfully",
            "requestId", id,
            "action", data.getOrDefault("action", "APPROVE")
        );
    }
    
    public List<Map<String,Object>> getSuspiciousAccounts(){
        List<Map<String,Object>> accounts = new ArrayList<>();
        accounts.add(Map.of(
            "accountId", "ACC123456",
            "customerName", "Suspicious Customer",
            "riskScore", 85,
            "flagReason", "Unusual transaction patterns",
            "lastActivity", LocalDateTime.now().minusHours(2).toString(),
            "status", "UNDER_REVIEW"
        ));
        return accounts;
    }
    
    public Map<String,Object> investigateAccount(String id, Map<String,String> data){
        return Map.of(
            "success", true,
            "message", "Account investigation initiated",
            "accountId", id,
            "notes", data.getOrDefault("notes", "")
        );
    }

    // Transaction Authorization Methods
    public List<Map<String,Object>> getPendingTransactions(){
        List<Map<String,Object>> transactions = new ArrayList<>();
        transactions.add(Map.of(
            "id", "TXN001",
            "fromAccount", "****5678",
            "toAccount", "****9012",
            "amount", 250000,
            "type", "WIRE_TRANSFER",
            "status", "PENDING_MANAGER_APPROVAL",
            "submittedBy", "Suresh Patel",
            "riskScore", 75
        ));
        return transactions;
    }
    
    public Map<String,Object> authorizeTransaction(String id, String action){
        return Map.of(
            "success", true,
            "message", "Transaction " + action.toLowerCase() + "ed successfully",
            "transactionId", id,
            "action", action
        );
    }
    
    public List<Map<String,Object>> getChargebacks(){
        List<Map<String,Object>> chargebacks = new ArrayList<>();
        chargebacks.add(Map.of(
            "id", "CB001",
            "customerName", "Priya Sharma",
            "amount", 15000,
            "reason", "Product not delivered",
            "status", "INVESTIGATING",
            "merchant", "Online Store",
            "submittedDate", LocalDateTime.now().minusDays(2).toString()
        ));
        return chargebacks;
    }
    
    public Map<String,Object> processChargeback(String id, Map<String,Object> data){
        return Map.of(
            "success", true,
            "message", "Chargeback processed successfully",
            "chargebackId", id,
            "action", data.getOrDefault("action", "APPROVE")
        );
    }

    // Compliance & Risk Management Methods
    public List<Map<String,Object>> getKycCases(){
        List<Map<String,Object>> cases = new ArrayList<>();
        cases.add(Map.of(
            "caseId", "KYC001",
            "customerName", "International Trading Corp",
            "riskLevel", "HIGH",
            "status", "PENDING_REVIEW",
            "complianceScore", 65,
            "dueDate", LocalDateTime.now().plusDays(7).toString()
        ));
        return cases;
    }
    
    public Map<String,Object> processKycCase(String id, Map<String,String> data){
        return Map.of(
            "success", true,
            "message", "KYC case processed successfully",
            "caseId", id,
            "action", data.getOrDefault("action", "APPROVE")
        );
    }
    
    public List<Map<String,Object>> getComplianceReports(){
        List<Map<String,Object>> reports = new ArrayList<>();
        reports.add(Map.of(
            "reportId", "SAR001",
            "reportType", "SAR_FILING",
            "status", "PENDING_SUBMISSION",
            "dueDate", LocalDateTime.now().plusDays(15).toString(),
            "priority", "HIGH"
        ));
        return reports;
    }
    
    public Map<String,Object> submitComplianceReport(String id){
        return Map.of(
            "success", true,
            "message", "Compliance report submitted successfully",
            "reportId", id
        );
    }
    
    public List<Map<String,Object>> getAuditIssues(){
        List<Map<String,Object>> issues = new ArrayList<>();
        issues.add(Map.of(
            "issueId", "AUDIT001",
            "title", "Transaction monitoring delays",
            "severity", "HIGH",
            "status", "OPEN",
            "progress", 30,
            "dueDate", LocalDateTime.now().plusDays(17).toString()
        ));
        return issues;
    }
    
    public Map<String,Object> resolveAuditIssue(String id, Map<String,String> data){
        return Map.of(
            "success", true,
            "message", "Audit issue resolved successfully",
            "issueId", id,
            "notes", data.getOrDefault("notes", "")
        );
    }

    // Reporting & Monitoring Methods
    public List<Map<String,Object>> getOperationalReports(){
        List<Map<String,Object>> reports = new ArrayList<>();
        reports.add(Map.of(
            "reportName", "Daily Transaction Summary",
            "reportType", "OPERATIONAL",
            "generatedDate", LocalDateTime.now().toString(),
            "totalTransactions", 15847,
            "totalValue", 89456789
        ));
        return reports;
    }
    
    public Map<String,Object> generateReport(Map<String,String> data){
        return Map.of(
            "success", true,
            "message", "Report generated successfully",
            "reportType", data.getOrDefault("type", "CUSTOM"),
            "reportUrl", "/reports/custom_" + System.currentTimeMillis() + ".pdf"
        );
    }
    
    public List<Map<String,Object>> getFraudAlerts(){
        List<Map<String,Object>> alerts = new ArrayList<>();
        alerts.add(Map.of(
            "alertId", "FRD001",
            "customerName", "Rajesh Kumar",
            "alertType", "SUSPICIOUS_TRANSACTION",
            "severity", "HIGH",
            "riskScore", 85,
            "amount", 250000,
            "status", "INVESTIGATING"
        ));
        return alerts;
    }
    
    public Map<String,Object> processFraudAlert(String id, String action){
        return Map.of(
            "success", true,
            "message", "Fraud alert " + action.toLowerCase() + "ed successfully",
            "alertId", id,
            "action", action
        );
    }

    // System Access & Security Methods
    public List<Map<String,Object>> getSystemAccess(){
        List<Map<String,Object>> systems = new ArrayList<>();
        systems.add(Map.of(
            "systemName", "Core Banking System",
            "systemCode", "CBS-01",
            "accessLevel", "MANAGER",
            "status", "ACTIVE",
            "riskLevel", "HIGH"
        ));
        return systems;
    }
    
    public Map<String,Object> manageSystemAccess(String id, String action){
        return Map.of(
            "success", true,
            "message", "System access " + action.toLowerCase() + "ed successfully",
            "systemId", id,
            "action", action
        );
    }
    
    public List<Map<String,Object>> getTeamAccess(){
        List<Map<String,Object>> teamAccess = new ArrayList<>();
        teamAccess.add(Map.of(
            "employeeId", "EMP001",
            "employeeName", "Suresh Patel",
            "accessLevel", "STAFF",
            "status", "ACTIVE",
            "lastLogin", LocalDateTime.now().minusHours(1).toString(),
            "mfaEnabled", true
        ));
        return teamAccess;
    }
    
    public Map<String,Object> manageEmployeeAccess(String id, String action){
        return Map.of(
            "success", true,
            "message", "Employee access " + action.toLowerCase() + "ed successfully",
            "employeeId", id,
            "action", action
        );
    }
    
    public List<Map<String,Object>> getSecurityLogs(){
        List<Map<String,Object>> logs = new ArrayList<>();
        logs.add(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "eventType", "LOGIN_SUCCESS",
            "severity", "INFO",
            "userId", "EMP001",
            "userName", "Suresh Patel",
            "ipAddress", "192.168.1.45"
        ));
        return logs;
    }

    // Customer Issue Resolution Methods
    public List<Map<String,Object>> getComplaints(){
        List<Map<String,Object>> complaints = new ArrayList<>();
        complaints.add(Map.of(
            "complaintId", "CMP001",
            "customerName", "Rajesh Kumar",
            "complaintType", "TRANSACTION_DISPUTE",
            "priority", "HIGH",
            "status", "ESCALATED",
            "dueDate", LocalDateTime.now().plusDays(1).toString()
        ));
        return complaints;
    }
    
    public Map<String,Object> processComplaint(String id, Map<String,Object> data){
        return Map.of(
            "success", true,
            "message", "Complaint processed successfully",
            "complaintId", id,
            "action", data.getOrDefault("action", "RESOLVE")
        );
    }
    
    public List<Map<String,Object>> getDisputes(){
        List<Map<String,Object>> disputes = new ArrayList<>();
        disputes.add(Map.of(
            "disputeId", "DSP001",
            "customerName", "Sunita Patel",
            "disputeType", "CHARGEBACK",
            "amount", 15000,
            "status", "INVESTIGATING",
            "priority", "HIGH"
        ));
        return disputes;
    }
    
    public Map<String,Object> processDispute(String id, Map<String,Object> data){
        return Map.of(
            "success", true,
            "message", "Dispute processed successfully",
            "disputeId", id,
            "action", data.getOrDefault("action", "RESOLVE")
        );
    }
    
    public List<Map<String,Object>> getEscalations(){
        List<Map<String,Object>> escalations = new ArrayList<>();
        escalations.add(Map.of(
            "ticketId", "ESC001",
            "customerName", "Rajesh Kumar",
            "issueType", "TRANSACTION_DISPUTE",
            "urgency", "HIGH",
            "escalatedDate", LocalDateTime.now().minusHours(8).toString(),
            "actionRequired", "INVESTIGATION_AND_APPROVAL"
        ));
        return escalations;
    }
    
    public Map<String,Object> processEscalation(String id, Map<String,Object> data){
        return Map.of(
            "success", true,
            "message", "Escalation processed successfully",
            "escalationId", id,
            "action", data.getOrDefault("action", "UPDATE")
        );
    }

    // Legacy methods for backward compatibility
    public List<LoanApplication> getPendingLoans(){ 
        return loanRepository.findByStatus(LoanStatus.PENDING); 
    }
    
    public Map<String,Object> approveLoan(String loanId, String decision){
        LoanApplication loan = loanRepository.findById(Long.parseLong(loanId)).orElseThrow();
        if ("APPROVE".equalsIgnoreCase(decision)) {
            loan.setStatus(LoanStatus.APPROVED);
        } else {
            loan.setStatus(LoanStatus.REJECTED);
        }
        loanRepository.save(loan);
        return Map.of("loanId", loan.getId(), "status", loan.getStatus().name());
    }
    
    public Map<String,Object> branchReports(){ 
        return Map.of("pendingLoans", getPendingLoans().size()); 
    }
    
    public List<Map<String,Object>> employeePerformance(){ 
        return getStaffPerformance(); 
    }
}

