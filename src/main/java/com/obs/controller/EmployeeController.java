package com.obs.controller;

import com.obs.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Employee Controller - Customer service and operational endpoints
 * Enhanced with comprehensive security and role-based access control
 * Accessible by users with EMPLOYEE, MANAGER, or ADMIN roles
 */
@RestController
@RequestMapping("/employee")
@PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;
    
    public EmployeeController(EmployeeService employeeService){ 
        this.employeeService = employeeService; 
    }

    // KYC Processing Endpoints
    @GetMapping("/kyc/pending") 
    public ResponseEntity<?> pendingKyc(){ 
        return ResponseEntity.ok(employeeService.pendingKyc()); 
    }
    
    @PostMapping("/kyc/{id}/process") 
    public ResponseEntity<?> processKyc(@PathVariable String id, @RequestBody java.util.Map<String,String> body){ 
        return ResponseEntity.ok(employeeService.processKyc(id, body.getOrDefault("decision","PENDING"), body.getOrDefault("comments",""))); 
    }
    
    // Transaction Processing Endpoints
    @GetMapping("/transactions/pending") 
    public ResponseEntity<?> pendingTx(){ 
        return ResponseEntity.ok(employeeService.pendingTransactions()); 
    }
    
    @PostMapping("/transactions/{id}/flag") 
    public ResponseEntity<?> flag(@PathVariable String id, @RequestBody java.util.Map<String,String> body){ 
        return ResponseEntity.ok(employeeService.flagTransaction(id, body.getOrDefault("reason",""))); 
    }
    
    // Account Processing Endpoints
    @GetMapping("/accounts/pending") 
    public ResponseEntity<?> pendingAccounts(){ 
        return ResponseEntity.ok(employeeService.pendingAccounts()); 
    }
    
    @PostMapping("/accounts/{id}/process") 
    public ResponseEntity<?> processAccount(@PathVariable String id, @RequestBody java.util.Map<String,String> body){ 
        return ResponseEntity.ok(employeeService.processAccount(id, body.getOrDefault("decision","PENDING"))); 
    }
}
