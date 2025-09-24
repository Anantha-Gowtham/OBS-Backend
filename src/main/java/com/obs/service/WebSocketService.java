package com.obs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send real-time updates to admin dashboard
     */
    public void sendAdminUpdate(String updateType, Map<String, Object> data) {
        messagingTemplate.convertAndSend("/topic/admin-updates", Map.of(
            "type", updateType,
            "data", data,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Send real-time updates to manager dashboard
     */
    public void sendManagerUpdate(String updateType, Map<String, Object> data) {
        messagingTemplate.convertAndSend("/topic/manager-updates", Map.of(
            "type", updateType,
            "data", data,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Send real-time updates to employee dashboard
     */
    public void sendEmployeeUpdate(String updateType, Map<String, Object> data) {
        messagingTemplate.convertAndSend("/topic/employee-updates", Map.of(
            "type", updateType,
            "data", data,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Send notification to specific user
     */
    public void sendUserNotification(String username, String message, String type) {
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", Map.of(
            "message", message,
            "type", type,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Send transaction update
     */
    public void sendTransactionUpdate(Map<String, Object> transaction) {
        // Send to admin dashboard
        sendAdminUpdate("TRANSACTION", Map.of(
            "transaction", transaction,
            "action", "NEW_TRANSACTION"
        ));

        // Send to manager dashboard
        sendManagerUpdate("TRANSACTION", Map.of(
            "transaction", transaction,
            "action", "NEW_TRANSACTION"
        ));
    }

    /**
     * Send user activity update
     */
    public void sendUserActivityUpdate(String username, String activity) {
        sendAdminUpdate("USER_ACTIVITY", Map.of(
            "username", username,
            "activity", activity,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Send account balance update
     */
    public void sendBalanceUpdate(String accountNumber, String newBalance, String username) {
        sendUserNotification(username, "Account balance updated", "BALANCE_UPDATE");
        
        sendAdminUpdate("BALANCE_UPDATE", Map.of(
            "accountNumber", accountNumber,
            "newBalance", newBalance,
            "username", username
        ));
    }

    /**
     * Send balance update with Long userId
     */
    public void sendBalanceUpdate(Long userId, String accountNumber, java.math.BigDecimal newBalance) {
        sendAdminUpdate("BALANCE_UPDATE", Map.of(
            "userId", userId,
            "accountNumber", accountNumber,
            "newBalance", newBalance,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Send user-specific update
     */
    public void sendUserUpdate(Long userId, String updateType, Map<String, Object> data) {
        messagingTemplate.convertAndSend("/topic/user-" + userId + "-updates", Map.of(
            "type", updateType,
            "data", data,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Send loan application update
     */
    public void sendLoanUpdate(Map<String, Object> loanData) {
        sendManagerUpdate("LOAN_APPLICATION", Map.of(
            "loan", loanData,
            "action", "NEW_APPLICATION"
        ));

        sendAdminUpdate("LOAN_APPLICATION", Map.of(
            "loan", loanData,
            "action", "NEW_APPLICATION"
        ));
    }

    /**
     * Send system alert
     */
    public void sendSystemAlert(String alertType, String message, Object data) {
        Map<String, Object> alert = Map.of(
            "alertType", alertType,
            "message", message,
            "data", data != null ? data : Map.of(),
            "severity", getSeverityLevel(alertType),
            "timestamp", System.currentTimeMillis()
        );

        // Send to all admin users
        messagingTemplate.convertAndSend("/topic/system-alerts", alert);
    }

    private String getSeverityLevel(String alertType) {
        switch (alertType.toLowerCase()) {
            case "security_breach":
            case "fraud_detection":
                return "CRITICAL";
            case "high_value_transaction":
            case "account_locked":
                return "HIGH";
            case "failed_login":
            case "password_reset":
                return "MEDIUM";
            default:
                return "LOW";
        }
    }
}