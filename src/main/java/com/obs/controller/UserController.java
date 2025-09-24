package com.obs.controller;

import com.obs.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * User Controller - Customer banking operations endpoints
 * Enhanced with comprehensive security and role-based access control
 * Accessible by users with USER, EMPLOYEE, MANAGER, or ADMIN roles
 */
@RestController
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('USER', 'EMPLOYEE', 'MANAGER', 'ADMIN')")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    
    public UserController(UserService userService){ 
        this.userService = userService; 
    }

    // Account Access Endpoints
    @GetMapping("/accounts") 
    public ResponseEntity<?> accounts(){ 
        return ResponseEntity.ok(userService.accounts()); 
    }
    
    @GetMapping("/accounts/{id}") 
    public ResponseEntity<?> getAccountDetails(@PathVariable String id){ 
        return ResponseEntity.ok(userService.getAccountDetails(id)); 
    }
    
    @GetMapping("/accounts/{id}/balance") 
    public ResponseEntity<?> getAccountBalance(@PathVariable String id){ 
        return ResponseEntity.ok(userService.getAccountBalance(id)); 
    }
    
    @GetMapping("/accounts/{id}/transactions") 
    public ResponseEntity<?> transactions(@PathVariable("id") String id, 
                                          @RequestParam(defaultValue = "0") int page, 
                                          @RequestParam(defaultValue = "10") int size){ 
        return ResponseEntity.ok(userService.transactions(id,page,size)); 
    }
    
    @GetMapping("/accounts/{id}/statements") 
    public ResponseEntity<?> getStatements(@PathVariable String id, 
                                           @RequestParam(required = false) String fromDate,
                                           @RequestParam(required = false) String toDate){ 
        return ResponseEntity.ok(userService.getAccountStatements(id, fromDate, toDate)); 
    }
    
    @PostMapping("/accounts/{id}/statements/download") 
    public ResponseEntity<?> downloadStatement(@PathVariable String id, @RequestBody Map<String,String> data){ 
        return ResponseEntity.ok(userService.downloadStatement(id, data)); 
    }

    // Fund Transfer Endpoints
    @PostMapping("/transfer") 
    public ResponseEntity<?> transfer(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.transfer(body)); 
    }
    
    @PostMapping("/transfer/internal") 
    public ResponseEntity<?> internalTransfer(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.internalTransfer(body)); 
    }
    
    @PostMapping("/transfer/external") 
    public ResponseEntity<?> externalTransfer(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.externalTransfer(body)); 
    }
    
    @PostMapping("/transfer/upi") 
    public ResponseEntity<?> upiTransfer(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.upiTransfer(body)); 
    }
    
    @PostMapping("/transfer/neft") 
    public ResponseEntity<?> neftTransfer(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.neftTransfer(body)); 
    }
    
    @PostMapping("/transfer/rtgs") 
    public ResponseEntity<?> rtgsTransfer(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.rtgsTransfer(body)); 
    }
    
    @GetMapping("/transfer/beneficiaries") 
    public ResponseEntity<?> getBeneficiaries(){ 
        return ResponseEntity.ok(userService.getBeneficiaries()); 
    }
    
    @PostMapping("/transfer/beneficiaries") 
    public ResponseEntity<?> addBeneficiary(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.addBeneficiary(body)); 
    }
    
    @DeleteMapping("/transfer/beneficiaries/{id}") 
    public ResponseEntity<?> deleteBeneficiary(@PathVariable String id){ 
        return ResponseEntity.ok(userService.deleteBeneficiary(id)); 
    }

    // Payments & Services Endpoints
    @GetMapping("/payments/bills") 
    public ResponseEntity<?> getBillPayments(){ 
        return ResponseEntity.ok(userService.getBillPayments()); 
    }
    
    @PostMapping("/payments/bills") 
    public ResponseEntity<?> payBill(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.payBill(body)); 
    }
    
    @PostMapping("/payments/mobile-recharge") 
    public ResponseEntity<?> mobileRecharge(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.mobileRecharge(body)); 
    }
    
    @PostMapping("/payments/utility") 
    public ResponseEntity<?> utilityPayment(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.utilityPayment(body)); 
    }
    
    @GetMapping("/payments/standing-instructions") 
    public ResponseEntity<?> getStandingInstructions(){ 
        return ResponseEntity.ok(userService.getStandingInstructions()); 
    }
    
    @PostMapping("/payments/standing-instructions") 
    public ResponseEntity<?> createStandingInstruction(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.createStandingInstruction(body)); 
    }
    
    @DeleteMapping("/payments/standing-instructions/{id}") 
    public ResponseEntity<?> cancelStandingInstruction(@PathVariable String id){ 
        return ResponseEntity.ok(userService.cancelStandingInstruction(id)); 
    }

    // Card Management Endpoints
    @GetMapping("/cards") 
    public ResponseEntity<?> getCards(){ 
        return ResponseEntity.ok(userService.getCards()); 
    }
    
    @GetMapping("/cards/{id}") 
    public ResponseEntity<?> getCardDetails(@PathVariable String id){ 
        return ResponseEntity.ok(userService.getCardDetails(id)); 
    }
    
    @PostMapping("/cards/{id}/block") 
    public ResponseEntity<?> blockCard(@PathVariable String id, @RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(userService.blockCard(id, body)); 
    }
    
    @PostMapping("/cards/{id}/unblock") 
    public ResponseEntity<?> unblockCard(@PathVariable String id){ 
        return ResponseEntity.ok(userService.unblockCard(id)); 
    }
    
    @PostMapping("/cards/{id}/change-pin") 
    public ResponseEntity<?> changeCardPin(@PathVariable String id, @RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(userService.changeCardPin(id, body)); 
    }
    
    @PostMapping("/cards/{id}/set-limits") 
    public ResponseEntity<?> setCardLimits(@PathVariable String id, @RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.setCardLimits(id, body)); 
    }
    
    @GetMapping("/cards/{id}/transactions") 
    public ResponseEntity<?> getCardTransactions(@PathVariable String id, 
                                                 @RequestParam(defaultValue = "") String fromDate,
                                                 @RequestParam(defaultValue = "") String toDate){ 
        return ResponseEntity.ok(userService.getCardTransactions(id, fromDate, toDate)); 
    }
    
    @PostMapping("/cards/apply") 
    public ResponseEntity<?> applyForCard(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.applyForCard(body)); 
    }

    // Loan & Investment Endpoints
    @GetMapping("/loans") 
    public ResponseEntity<?> loans(){ 
        return ResponseEntity.ok(userService.loans()); 
    }
    
    @PostMapping("/loans/apply") 
    public ResponseEntity<?> applyLoan(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.applyLoan(body)); 
    }
    
    @GetMapping("/loans/{id}") 
    public ResponseEntity<?> getLoanDetails(@PathVariable String id){ 
        return ResponseEntity.ok(userService.getLoanDetails(id)); 
    }
    
    @PostMapping("/loans/{id}/repay") 
    public ResponseEntity<?> repayLoan(@PathVariable String id, @RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.repayLoan(id, body)); 
    }
    
    @GetMapping("/loans/{id}/repayment-schedule") 
    public ResponseEntity<?> getLoanRepaymentSchedule(@PathVariable String id){ 
        return ResponseEntity.ok(userService.getLoanRepaymentSchedule(id)); 
    }
    
    @GetMapping("/investments/fixed-deposits") 
    public ResponseEntity<?> getFixedDeposits(){ 
        return ResponseEntity.ok(userService.getFixedDeposits()); 
    }
    
    @PostMapping("/investments/fixed-deposits") 
    public ResponseEntity<?> createFixedDeposit(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.createFixedDeposit(body)); 
    }
    
    @GetMapping("/investments/recurring-deposits") 
    public ResponseEntity<?> getRecurringDeposits(){ 
        return ResponseEntity.ok(userService.getRecurringDeposits()); 
    }
    
    @PostMapping("/investments/recurring-deposits") 
    public ResponseEntity<?> createRecurringDeposit(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.createRecurringDeposit(body)); 
    }
    
    @GetMapping("/investments/mutual-funds") 
    public ResponseEntity<?> getMutualFunds(){ 
        return ResponseEntity.ok(userService.getMutualFunds()); 
    }
    
    @PostMapping("/investments/mutual-funds/invest") 
    public ResponseEntity<?> investInMutualFund(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.investInMutualFund(body)); 
    }

    // Profile & Security Endpoints
    @GetMapping("/profile") 
    public ResponseEntity<?> getProfile(){ 
        return ResponseEntity.ok(userService.getProfile()); 
    }
    
    @PutMapping("/profile") 
    public ResponseEntity<?> updateProfile(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.updateProfile(body)); 
    }
    
    @PostMapping("/change-password") 
    public ResponseEntity<?> changePassword(@RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(userService.changePassword(body.getOrDefault("oldPassword",""), body.getOrDefault("newPassword",""))); 
    }
    
    @PostMapping("/enable-2fa") 
    public ResponseEntity<?> enableTwoFactorAuth(@RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(userService.enableTwoFactorAuth(body)); 
    }
    
    @PostMapping("/disable-2fa") 
    public ResponseEntity<?> disableTwoFactorAuth(@RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(userService.disableTwoFactorAuth(body)); 
    }
    
    @GetMapping("/security/settings") 
    public ResponseEntity<?> getSecuritySettings(){ 
        return ResponseEntity.ok(userService.getSecuritySettings()); 
    }
    
    @PostMapping("/security/settings") 
    public ResponseEntity<?> updateSecuritySettings(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.updateSecuritySettings(body)); 
    }
    
    @GetMapping("/login-history") 
    public ResponseEntity<?> loginHistory(){ 
        return ResponseEntity.ok(userService.loginHistory()); 
    }
    
    @GetMapping("/security/devices") 
    public ResponseEntity<?> getTrustedDevices(){ 
        return ResponseEntity.ok(userService.getTrustedDevices()); 
    }
    
    @PostMapping("/security/devices/{id}/revoke") 
    public ResponseEntity<?> revokeTrustedDevice(@PathVariable String id){ 
        return ResponseEntity.ok(userService.revokeTrustedDevice(id)); 
    }

    // Support & Service Requests Endpoints
    @GetMapping("/support/tickets") 
    public ResponseEntity<?> getSupportTickets(){ 
        return ResponseEntity.ok(userService.getSupportTickets()); 
    }
    
    @PostMapping("/support/tickets") 
    public ResponseEntity<?> createSupportTicket(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.createSupportTicket(body)); 
    }
    
    @GetMapping("/support/tickets/{id}") 
    public ResponseEntity<?> getSupportTicketDetails(@PathVariable String id){ 
        return ResponseEntity.ok(userService.getSupportTicketDetails(id)); 
    }
    
    @PostMapping("/support/tickets/{id}/reply") 
    public ResponseEntity<?> replySupportTicket(@PathVariable String id, @RequestBody Map<String,String> body){ 
        return ResponseEntity.ok(userService.replySupportTicket(id, body)); 
    }
    
    @PostMapping("/support/tickets/{id}/close") 
    public ResponseEntity<?> closeSupportTicket(@PathVariable String id){ 
        return ResponseEntity.ok(userService.closeSupportTicket(id)); 
    }
    
    @PostMapping("/fraud/report") 
    public ResponseEntity<?> reportFraud(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.reportFraud(body)); 
    }
    
    @GetMapping("/service-requests") 
    public ResponseEntity<?> getServiceRequests(){ 
        return ResponseEntity.ok(userService.getServiceRequests()); 
    }
    
    @PostMapping("/service-requests") 
    public ResponseEntity<?> createServiceRequest(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.createServiceRequest(body)); 
    }
    
    @GetMapping("/support/faq") 
    public ResponseEntity<?> getFAQ(){ 
        return ResponseEntity.ok(userService.getFAQ()); 
    }
    
    @PostMapping("/feedback") 
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.submitFeedback(body)); 
    }

    // Notifications & Alerts Endpoints
    @GetMapping("/notifications") 
    public ResponseEntity<?> getNotifications(){ 
        return ResponseEntity.ok(userService.getNotifications()); 
    }
    
    @PostMapping("/notifications/{id}/mark-read") 
    public ResponseEntity<?> markNotificationRead(@PathVariable String id){ 
        return ResponseEntity.ok(userService.markNotificationRead(id)); 
    }
    
    @PostMapping("/notifications/preferences") 
    public ResponseEntity<?> updateNotificationPreferences(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.updateNotificationPreferences(body)); 
    }
    
    @GetMapping("/alerts/account") 
    public ResponseEntity<?> getAccountAlerts(){ 
        return ResponseEntity.ok(userService.getAccountAlerts()); 
    }
    
    @PostMapping("/alerts/account") 
    public ResponseEntity<?> setAccountAlerts(@RequestBody Map<String,Object> body){ 
        return ResponseEntity.ok(userService.setAccountAlerts(body)); 
    }
}
