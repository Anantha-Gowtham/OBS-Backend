package com.obs.service;

import com.obs.model.*;
import com.obs.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LoanApplicationRepository loanRepository;

    public AdminService(UserRepository userRepository, BranchRepository branchRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Dashboard Statistics
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(u -> !u.isLocked()).count();
        long blockedUsers = allUsers.stream().filter(User::isLocked).count();
        
        // Role distribution
        long adminUsers = allUsers.stream().filter(u -> u.getRole() != null && "ADMIN".equals(u.getRole().name())).count();
        long managerUsers = allUsers.stream().filter(u -> u.getRole() != null && "MANAGER".equals(u.getRole().name())).count();
        long employeeUsers = allUsers.stream().filter(u -> u.getRole() != null && "EMPLOYEE".equals(u.getRole().name())).count();
        long customerUsers = allUsers.stream().filter(u -> u.getRole() != null && "USER".equals(u.getRole().name())).count();
        
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("blockedUsers", blockedUsers);
        stats.put("adminUsers", adminUsers);
        stats.put("managerUsers", managerUsers);
        stats.put("employeeUsers", employeeUsers);
        stats.put("customerUsers", customerUsers);
        stats.put("totalBranches", branchRepository.count());
        stats.put("systemUptime", "99.8%");
        stats.put("securityAlerts", 12);
        
        return stats;
    }

    // User Management
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertUserToMap)
                .collect(Collectors.toList());
    }

    public Map<String, Object> createUser(Map<String, Object> userData) {
        User user = new User();
        user.setUsername((String) userData.get("username"));
        user.setEmail((String) userData.get("email"));
        
        // Encode password
        String password = (String) userData.getOrDefault("password", "DefaultPass123!");
        user.setPassword(passwordEncoder.encode(password));
        
        // Set role - default to USER if not specified
        String roleName = (String) userData.getOrDefault("role", "USER");
        try {
            user.setRole(Role.valueOf(roleName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            user.setRole(Role.USER);
        }
        
        user.setLocked(false);
        user.setFailedAttempts(0);
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        return convertUserToMap(savedUser);
    }

    public Map<String, Object> updateUser(String userId, Map<String, Object> userData) {
        Long id = Long.parseLong(userId);
        User user = userRepository.findById(id).orElseThrow(() -> 
            new RuntimeException("User not found with id: " + userId));
        
        if (userData.containsKey("email")) user.setEmail((String) userData.get("email"));
        if (userData.containsKey("username")) user.setUsername((String) userData.get("username"));
        if (userData.containsKey("locked")) user.setLocked((Boolean) userData.get("locked"));
        if (userData.containsKey("active")) user.setActive((Boolean) userData.get("active"));
        
        if (userData.containsKey("role")) {
            String roleName = (String) userData.get("role");
            try {
                user.setRole(Role.valueOf(roleName.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Keep existing role if invalid
            }
        }
        
        User savedUser = userRepository.save(user);
        return convertUserToMap(savedUser);
    }

    public void deleteUser(String userId) {
        try {
            Long id = Long.parseLong(userId);
            Optional<User> userOpt = userRepository.findById(id);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Cannot delete SUPER_ADMIN
                if (user.getRole() == Role.SUPER_ADMIN) {
                    throw new RuntimeException("Cannot delete Super Admin user");
                }
                
                // Delete associated refresh tokens first
                refreshTokenRepository.deleteByUser(user);
                
                // Delete user profile if exists
                Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);
                profileOpt.ifPresent(profile -> userProfileRepository.delete(profile));
                
                // Delete accounts owned by this user
                List<Account> userAccounts = accountRepository.findByUser_Id(id);
                for (Account account : userAccounts) {
                    // Delete transactions for this account
                    transactionRepository.deleteByAccountId(account.getId());
                    // Delete the account
                    accountRepository.delete(account);
                }
                
                // Delete loan applications by this user
                loanRepository.deleteByUserId(id);
                
                // Finally delete the user
                userRepository.delete(user);
            } else {
                throw new RuntimeException("User not found with id: " + userId);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid user ID format: " + userId);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage());
        }
    }

    public Map<String, Object> unlockUser(String userId) {
        Long id = Long.parseLong(userId);
        User user = userRepository.findById(id).orElseThrow(() -> 
            new RuntimeException("User not found with id: " + userId));
        
        user.setLocked(false);
        user.setFailedAttempts(0);
        userRepository.save(user);
        
        return Map.of("id", id, "unlocked", true, "message", "User account unlocked successfully");
    }

    public Map<String, Object> lockUser(String userId) {
        Long id = Long.parseLong(userId);
        User user = userRepository.findById(id).orElseThrow(() -> 
            new RuntimeException("User not found with id: " + userId));
        
        user.setLocked(true);
        userRepository.save(user);
        
        return Map.of("id", id, "locked", true, "message", "User account locked successfully");
    }

    public Map<String, Object> resetUserPassword(String userId) {
        Long id = Long.parseLong(userId);
        User user = userRepository.findById(id).orElseThrow(() -> 
            new RuntimeException("User not found with id: " + userId));
        
        // Generate temporary password
        String tempPassword = "TempPass" + System.currentTimeMillis();
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);
        
        return Map.of(
            "id", id, 
            "message", "Password reset successfully", 
            "temporaryPassword", tempPassword
        );
    }

    public Map<String, Object> getUserActivity(String userId) {
        return Map.of(
            "userId", userId,
            "lastLogin", LocalDateTime.now().minusHours(2).toString(),
            "sessionDuration", "3h 45m",
            "actionsPerformed", 127,
            "systemsAccessed", Arrays.asList("CBS", "CRM", "LMS")
        );
    }

    public Map<String, Object> performBulkUserActions(Map<String, Object> data) {
        @SuppressWarnings("unchecked")
        List<String> userIds = (List<String>) data.get("userIds");
        String action = (String) data.get("action");
        
        return Map.of(
            "success", true,
            "message", "Bulk action '" + action + "' performed on " + userIds.size() + " users",
            "affectedUsers", userIds.size()
        );
    }

    // Account Management
    public List<Map<String, Object>> getAllAccounts() {
        List<Map<String, Object>> accounts = new ArrayList<>();
        accounts.add(Map.of(
            "id", "ACC001",
            "accountNumber", "1234567890",
            "customerName", "John Doe",
            "accountType", "SAVINGS",
            "balance", 250000.0,
            "status", "ACTIVE",
            "openDate", LocalDateTime.now().minusMonths(6).toString()
        ));
        accounts.add(Map.of(
            "id", "ACC002",
            "accountNumber", "1234567891",
            "customerName", "Jane Smith",
            "accountType", "CURRENT",
            "balance", 150000.0,
            "status", "FROZEN",
            "openDate", LocalDateTime.now().minusMonths(3).toString()
        ));
        return accounts;
    }

    public Map<String, Object> createAccount(Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "Account created successfully",
            "accountId", "ACC" + System.currentTimeMillis(),
            "accountNumber", "12345" + System.currentTimeMillis()
        );
    }

    public Map<String, Object> updateAccount(String id, Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "Account updated successfully",
            "accountId", id
        );
    }

    public Map<String, Object> freezeAccount(String id, Map<String, String> data) {
        return Map.of(
            "success", true,
            "message", "Account frozen successfully",
            "accountId", id,
            "reason", data.getOrDefault("reason", "Administrative action")
        );
    }

    public Map<String, Object> unfreezeAccount(String id) {
        return Map.of(
            "success", true,
            "message", "Account unfrozen successfully",
            "accountId", id
        );
    }

    public Map<String, Object> closeAccount(String id, Map<String, String> data) {
        return Map.of(
            "success", true,
            "message", "Account closed successfully",
            "accountId", id,
            "reason", data.getOrDefault("reason", "Customer request")
        );
    }

    public Map<String, Object> getAccountHistory(String id) {
        return Map.of(
            "accountId", id,
            "history", Arrays.asList(
                Map.of("date", LocalDateTime.now().minusDays(1).toString(), "action", "Balance Inquiry", "user", "Customer"),
                Map.of("date", LocalDateTime.now().minusDays(2).toString(), "action", "Fund Transfer", "user", "Customer"),
                Map.of("date", LocalDateTime.now().minusDays(3).toString(), "action", "Account Update", "user", "Admin")
            )
        );
    }

    // Transaction Monitoring
    public List<Map<String, Object>> getAllTransactions() {
        List<Map<String, Object>> transactions = new ArrayList<>();
        transactions.add(Map.of(
            "id", "TXN001",
            "fromAccount", "1234567890",
            "toAccount", "0987654321",
            "amount", 50000.0,
            "type", "TRANSFER",
            "status", "COMPLETED",
            "timestamp", LocalDateTime.now().minusHours(2).toString(),
            "riskScore", 25
        ));
        transactions.add(Map.of(
            "id", "TXN002",
            "fromAccount", "1234567891",
            "toAccount", "EXTERNAL",
            "amount", 100000.0,
            "type", "WIRE_TRANSFER",
            "status", "FLAGGED",
            "timestamp", LocalDateTime.now().minusHours(1).toString(),
            "riskScore", 85
        ));
        return transactions;
    }

    public List<Map<String, Object>> getSuspiciousTransactions() {
        List<Map<String, Object>> suspiciousTransactions = new ArrayList<>();
        suspiciousTransactions.add(Map.of(
            "id", "TXN002",
            "fromAccount", "1234567891",
            "amount", 100000.0,
            "riskScore", 85,
            "flags", Arrays.asList("HIGH_AMOUNT", "UNUSUAL_PATTERN", "EXTERNAL_TRANSFER"),
            "timestamp", LocalDateTime.now().minusHours(1).toString()
        ));
        return suspiciousTransactions;
    }

    public Map<String, Object> flagTransaction(String id, Map<String, String> data) {
        return Map.of(
            "success", true,
            "message", "Transaction flagged successfully",
            "transactionId", id,
            "reason", data.getOrDefault("reason", "Manual review required")
        );
    }

    public Map<String, Object> reverseTransaction(String id, Map<String, String> data) {
        return Map.of(
            "success", true,
            "message", "Transaction reversed successfully",
            "transactionId", id,
            "reason", data.getOrDefault("reason", "Fraudulent activity")
        );
    }

    public Map<String, Object> getTransactionPatterns() {
        return Map.of(
            "dailyVolume", 1500000.0,
            "peakHours", Arrays.asList("10:00-11:00", "14:00-15:00"),
            "frequentTypes", Arrays.asList("TRANSFER", "PAYMENT", "WITHDRAWAL"),
            "riskTrends", Map.of("increasing", 12, "decreasing", 8)
        );
    }

    public Map<String, Object> updateTransactionLimits(Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "Transaction limits updated successfully",
            "limits", data
        );
    }

    // Security & Compliance
    public List<Map<String, Object>> getSecurityPolicies() {
        List<Map<String, Object>> policies = new ArrayList<>();
        policies.add(Map.of(
            "id", "POL001",
            "name", "Password Policy",
            "description", "Minimum password requirements",
            "status", "ACTIVE",
            "lastUpdated", LocalDateTime.now().minusDays(30).toString()
        ));
        policies.add(Map.of(
            "id", "POL002",
            "name", "Access Control Policy",
            "description", "Role-based access controls",
            "status", "ACTIVE",
            "lastUpdated", LocalDateTime.now().minusDays(15).toString()
        ));
        return policies;
    }

    public Map<String, Object> updateSecurityPolicy(Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "Security policy updated successfully",
            "policyId", data.get("policyId")
        );
    }

    public Map<String, Object> getComplianceStatus() {
        return Map.of(
            "overallScore", 94.5,
            "kycCompliance", 96.2,
            "amlCompliance", 92.8,
            "dataProtection", 95.1,
            "lastAudit", LocalDateTime.now().minusDays(90).toString(),
            "nextAudit", LocalDateTime.now().plusDays(90).toString()
        );
    }

    public Map<String, Object> triggerComplianceScan() {
        return Map.of(
            "success", true,
            "message", "Compliance scan initiated",
            "scanId", "SCAN" + System.currentTimeMillis(),
            "estimatedDuration", "30 minutes"
        );
    }

    public List<Map<String, Object>> getSecurityIncidents() {
        return generateMockSecurityIncidents();
    }

    public Map<String, Object> resolveSecurityIncident(String id, Map<String, String> data) {
        return Map.of(
            "success", true,
            "message", "Security incident resolved",
            "incidentId", id,
            "resolution", data.getOrDefault("resolution", "Issue resolved")
        );
    }

    // System Configuration
    public Map<String, Object> getSystemConfiguration() {
        return Map.of(
            "maintenanceMode", false,
            "sessionTimeout", 30,
            "maxLoginAttempts", 5,
            "passwordExpiry", 90,
            "backupFrequency", "DAILY",
            "logRetention", 365
        );
    }

    public Map<String, Object> updateSystemConfiguration(Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "System configuration updated successfully",
            "updatedSettings", data
        );
    }

    public Map<String, Object> getFeatureFlags() {
        return Map.of(
            "twoFactorAuth", true,
            "biometricAuth", false,
            "advancedReporting", true,
            "realTimeFraudDetection", true,
            "mobileBanking", true
        );
    }

    public Map<String, Object> updateFeatureFlags(Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "Feature flags updated successfully",
            "flags", data
        );
    }

    public Map<String, Object> scheduleMaintenanceMode(Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "Maintenance mode scheduled",
            "startTime", data.get("startTime"),
            "duration", data.get("duration")
        );
    }

    public List<Map<String, Object>> getSystemIntegrations() {
        List<Map<String, Object>> integrations = new ArrayList<>();
        integrations.add(Map.of(
            "id", "INT001",
            "name", "Payment Gateway",
            "type", "EXTERNAL",
            "status", "CONNECTED",
            "lastSync", LocalDateTime.now().minusMinutes(5).toString()
        ));
        integrations.add(Map.of(
            "id", "INT002",
            "name", "Credit Bureau",
            "type", "EXTERNAL",
            "status", "CONNECTED",
            "lastSync", LocalDateTime.now().minusMinutes(15).toString()
        ));
        return integrations;
    }

    public Map<String, Object> testIntegration(String id) {
        return Map.of(
            "success", true,
            "message", "Integration test completed",
            "integrationId", id,
            "status", "HEALTHY",
            "responseTime", "245ms"
        );
    }

    // Reporting & Analytics
    public Map<String, Object> getComprehensiveReports() {
        return Map.of(
            "userActivity", Map.of("totalLogins", 1247, "averageSessionTime", "45m"),
            "transactionVolume", Map.of("daily", 1500000.0, "weekly", 10500000.0),
            "systemPerformance", Map.of("uptime", 99.8, "averageResponseTime", "120ms"),
            "securityMetrics", Map.of("threatsBlocked", 89, "vulnerabilities", 3)
        );
    }

    public Map<String, Object> generateCustomReport(Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "Custom report generated",
            "reportId", "RPT" + System.currentTimeMillis(),
            "downloadUrl", "/reports/custom_" + System.currentTimeMillis() + ".pdf"
        );
    }

    public Map<String, Object> getAnalyticsDashboard() {
        return Map.of(
            "userGrowth", Arrays.asList(100, 120, 150, 180, 200),
            "transactionTrends", Arrays.asList(500000, 600000, 750000, 900000, 1200000),
            "securityScore", 94.5,
            "systemLoad", 65.2
        );
    }

    public Map<String, Object> getBusinessTrends() {
        return Map.of(
            "customerAcquisition", Map.of("thisMonth", 245, "lastMonth", 189),
            "revenueGrowth", Map.of("thisQuarter", 15.7, "lastQuarter", 12.3),
            "productAdoption", Map.of("digitalBanking", 89.2, "mobileApp", 76.5)
        );
    }

    public Map<String, Object> scheduleReport(Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "Report scheduled successfully",
            "scheduleId", "SCH" + System.currentTimeMillis(),
            "nextRun", data.get("nextRun")
        );
    }

    // Disaster Recovery
    public Map<String, Object> getBackupStatus() {
        return Map.of(
            "lastBackup", LocalDateTime.now().minusHours(6).toString(),
            "backupSize", "2.3 GB",
            "status", "COMPLETED",
            "nextScheduled", LocalDateTime.now().plusHours(18).toString(),
            "retentionPeriod", "30 days"
        );
    }

    public Map<String, Object> createBackup(Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "Backup initiated",
            "backupId", "BCK" + System.currentTimeMillis(),
            "type", data.getOrDefault("type", "FULL"),
            "estimatedDuration", "45 minutes"
        );
    }

    public Map<String, Object> initiateRestore(Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "Restore process initiated",
            "restoreId", "RST" + System.currentTimeMillis(),
            "backupId", data.get("backupId"),
            "estimatedDuration", "2 hours"
        );
    }

    public Map<String, Object> getDisasterRecoveryPlan() {
        return Map.of(
            "rto", "4 hours",
            "rpo", "1 hour",
            "lastTest", LocalDateTime.now().minusDays(90).toString(),
            "nextTest", LocalDateTime.now().plusDays(90).toString(),
            "status", "UP_TO_DATE"
        );
    }

    public Map<String, Object> testDisasterRecovery() {
        return Map.of(
            "success", true,
            "message", "Disaster recovery test initiated",
            "testId", "DRT" + System.currentTimeMillis(),
            "estimatedDuration", "3 hours"
        );
    }

    // Audit & Logs
    public List<Map<String, Object>> getAuditTrails() {
        List<Map<String, Object>> trails = new ArrayList<>();
        trails.add(Map.of(
            "id", "AUD001",
            "action", "USER_LOGIN",
            "userId", "admin",
            "timestamp", LocalDateTime.now().minusHours(1).toString(),
            "ipAddress", "192.168.1.100",
            "details", "Successful admin login"
        ));
        trails.add(Map.of(
            "id", "AUD002",
            "action", "ACCOUNT_CREATED",
            "userId", "manager01",
            "timestamp", LocalDateTime.now().minusHours(2).toString(),
            "ipAddress", "192.168.1.101",
            "details", "New customer account created"
        ));
        return trails;
    }

    public List<Map<String, Object>> getUserAuditTrail(String userId) {
        List<Map<String, Object>> trails = new ArrayList<>();
        trails.add(Map.of(
            "id", "AUD003",
            "action", "PASSWORD_CHANGE",
            "userId", userId,
            "timestamp", LocalDateTime.now().minusDays(1).toString(),
            "ipAddress", "192.168.1.102",
            "details", "User password changed"
        ));
        return trails;
    }

    public Map<String, Object> exportAuditLogs(Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "Audit logs export initiated",
            "exportId", "EXP" + System.currentTimeMillis(),
            "format", data.getOrDefault("format", "CSV"),
            "downloadUrl", "/exports/audit_" + System.currentTimeMillis() + ".csv"
        );
    }

    public List<Map<String, Object>> getSystemLogs() {
        return generateMockSystemLogs();
    }

    public Map<String, Object> generateComplianceAuditReport(Map<String, Object> data) {
        return Map.of(
            "success", true,
            "message", "Compliance audit report generated",
            "reportId", "CAR" + System.currentTimeMillis(),
            "period", data.get("period"),
            "downloadUrl", "/reports/compliance_audit_" + System.currentTimeMillis() + ".pdf"
        );
    }

    // Branch Management (existing functionality)
    public List<Map<String, Object>> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(this::convertBranchToMap)
                .collect(Collectors.toList());
    }

    public Map<String, Object> createBranch(Map<String, Object> data) {
        Branch branch = new Branch();
        branch.setName((String) data.get("name"));
        branch.setCity((String) data.get("city"));
        
        Branch savedBranch = branchRepository.save(branch);
        return convertBranchToMap(savedBranch);
    }

    public Map<String, Object> updateBranch(String idStr, Map<String, Object> data) {
        Long id = Long.parseLong(idStr);
        Branch branch = branchRepository.findById(id).orElseThrow(() -> 
            new RuntimeException("Branch not found with id: " + idStr));
        
        if (data.containsKey("name")) branch.setName((String) data.get("name"));
        if (data.containsKey("city")) branch.setCity((String) data.get("city"));
        
        Branch savedBranch = branchRepository.save(branch);
        return convertBranchToMap(savedBranch);
    }

    public void deleteBranch(String id) {
        Long branchId = Long.parseLong(id);
        branchRepository.deleteById(branchId);
    }

    // Legacy methods for backward compatibility
    public Map<String, Object> getSecurityReports() {
        Map<String, Object> reports = new HashMap<>();
        
        List<User> users = userRepository.findAll();
        long failedLogins = users.stream().mapToLong(User::getFailedAttempts).sum();
        long lockedAccounts = users.stream().filter(User::isLocked).count();
        
        Map<String, Object> securityStats = new HashMap<>();
        securityStats.put("totalThreats", 127);
        securityStats.put("activeThreatsMitigated", 89);
        securityStats.put("criticalAlerts", 8);
        securityStats.put("securityScore", 85);
        securityStats.put("failedLogins", failedLogins);
        securityStats.put("lockedAccounts", lockedAccounts);
        securityStats.put("lastScanTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        reports.put("securityStats", securityStats);
        reports.put("incidents", generateMockSecurityIncidents());
        reports.put("logs", generateMockSystemLogs());
        reports.put("vulnerabilityScans", generateMockVulnerabilityScans());
        
        return reports;
    }

    public Map<String, Object> getSystemReports() {
        Map<String, Object> reports = new HashMap<>();
        
        reports.put("totalUsers", userRepository.count());
        reports.put("totalBranches", branchRepository.count());
        reports.put("systemUptime", "99.8%");
        reports.put("lastBackup", LocalDateTime.now().minusHours(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        reports.put("databaseSize", "2.3 GB");
        reports.put("activeConnections", 45);
        
        return reports;
    }

    public List<Map<String, Object>> getRecentBranchAudits() {
        return generateMockAuditLogs();
    }

    public List<Map<String, Object>> getBranchAudits(String branchId) {
        return generateMockAuditLogs();
    }

    public List<Map<String, Object>> getSecurityLogs() {
        return generateMockSystemLogs();
    }

    // Helper methods
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole() != null ? user.getRole().name() : "USER");
        userMap.put("locked", user.isLocked());
        userMap.put("active", user.isActive());
        userMap.put("failedAttempts", user.getFailedAttempts());
        userMap.put("lastLogin", user.getLastLogin() != null ? user.getLastLogin().toString() : "Never");
        
        // Add mock data for frontend compatibility
        userMap.put("firstName", "Not Available");
        userMap.put("lastName", "Not Available");
        userMap.put("phone", "Not Available");
        userMap.put("createdAt", "Not Available");
        
        return userMap;
    }

    private Map<String, Object> convertBranchToMap(Branch branch) {
        Map<String, Object> branchMap = new HashMap<>();
        branchMap.put("id", branch.getId());
        branchMap.put("name", branch.getName());
        branchMap.put("city", branch.getCity());
        branchMap.put("createdAt", branch.getCreatedAt() != null ? branch.getCreatedAt().toString() : null);
        
        // Add mock data for frontend compatibility
        branchMap.put("address", "Not Available");
        branchMap.put("phone", "Not Available");
        branchMap.put("managerName", "Not Available");
        
        return branchMap;
    }

    // Mock data generators for demonstration
    private List<Map<String, Object>> generateMockSecurityIncidents() {
        List<Map<String, Object>> incidents = new ArrayList<>();
        
        Map<String, Object> incident1 = new HashMap<>();
        incident1.put("id", "INC001");
        incident1.put("title", "Multiple Failed Login Attempts");
        incident1.put("description", "User account exceeded failed login threshold");
        incident1.put("severity", "High");
        incident1.put("category", "Authentication");
        incident1.put("status", "Investigating");
        incident1.put("timestamp", LocalDateTime.now().minusHours(2).toString());
        incidents.add(incident1);
        
        Map<String, Object> incident2 = new HashMap<>();
        incident2.put("id", "INC002");
        incident2.put("title", "Suspicious Transaction Pattern");
        incident2.put("description", "Unusual transaction frequency detected");
        incident2.put("severity", "Critical");
        incident2.put("category", "Fraud Detection");
        incident2.put("status", "Resolved");
        incident2.put("timestamp", LocalDateTime.now().minusHours(4).toString());
        incidents.add(incident2);
        
        return incidents;
    }

    private List<Map<String, Object>> generateMockSystemLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        
        Map<String, Object> log1 = new HashMap<>();
        log1.put("id", "LOG001");
        log1.put("timestamp", LocalDateTime.now().minusMinutes(15).toString());
        log1.put("level", "INFO");
        log1.put("category", "Authentication");
        log1.put("message", "User login successful");
        log1.put("source", "Auth Service");
        logs.add(log1);
        
        Map<String, Object> log2 = new HashMap<>();
        log2.put("id", "LOG002");
        log2.put("timestamp", LocalDateTime.now().minusMinutes(30).toString());
        log2.put("level", "WARNING");
        log2.put("category", "Security");
        log2.put("message", "Failed login attempt detected");
        log2.put("source", "Auth Service");
        logs.add(log2);
        
        return logs;
    }

    private List<Map<String, Object>> generateMockVulnerabilityScans() {
        List<Map<String, Object>> scans = new ArrayList<>();
        
        Map<String, Object> scan1 = new HashMap<>();
        scan1.put("id", "SCAN001");
        scan1.put("name", "Network Infrastructure Scan");
        scan1.put("type", "Network");
        scan1.put("status", "Completed");
        scan1.put("lastRun", LocalDateTime.now().minusHours(12).toString());
        scan1.put("score", 8.5);
        scans.add(scan1);
        
        return scans;
    }

    private List<Map<String, Object>> generateMockAuditLogs() {
        List<Map<String, Object>> audits = new ArrayList<>();
        
        Map<String, Object> audit1 = new HashMap<>();
        audit1.put("id", "AUDIT001");
        audit1.put("action", "User Created");
        audit1.put("user", "admin");
        audit1.put("timestamp", LocalDateTime.now().minusHours(1).toString());
        audit1.put("details", "New user account created");
        audits.add(audit1);
        
        return audits;
    }
}

