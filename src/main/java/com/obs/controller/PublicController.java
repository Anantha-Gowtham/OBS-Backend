package com.obs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Public Controller - Public information and services endpoints
 * Accessible without authentication for landing page and service information
 */
@RestController
@RequestMapping("/public")
@CrossOrigin(origins = "*")
public class PublicController {

    /**
     * Get banking services information
     */
    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getBankingServices() {
        Map<String, Object> services = new HashMap<>();
        
        services.put("personalBanking", List.of(
            Map.of(
                "name", "Savings Account",
                "description", "Earn interest on your savings with our competitive rates",
                "features", List.of("Zero balance maintenance", "24/7 online banking", "Free debit card"),
                "interestRate", "3.5%"
            ),
            Map.of(
                "name", "Current Account",
                "description", "Business banking solutions for your enterprise",
                "features", List.of("High transaction limits", "Overdraft facility", "Business debit card"),
                "interestRate", "1.5%"
            ),
            Map.of(
                "name", "Fixed Deposit",
                "description", "Secure your future with guaranteed returns",
                "features", List.of("Competitive interest rates", "Flexible tenure", "Loan against FD"),
                "interestRate", "6.5%"
            )
        ));
        
        services.put("digitalBanking", List.of(
            Map.of(
                "name", "Online Banking",
                "description", "Complete banking at your fingertips",
                "features", List.of("Fund transfers", "Bill payments", "Account statements", "Investment tracking")
            ),
            Map.of(
                "name", "Mobile Banking",
                "description", "Bank on the go with our mobile app",
                "features", List.of("QR code payments", "Cardless ATM", "Quick balance check", "Mini statements")
            ),
            Map.of(
                "name", "UPI Services",
                "description", "Instant payments with UPI",
                "features", List.of("24x7 availability", "Zero transaction charges", "Multiple bank support")
            )
        ));
        
        services.put("loans", List.of(
            Map.of(
                "name", "Personal Loan",
                "description", "Meet your personal financial needs",
                "features", List.of("Quick approval", "Minimal documentation", "Competitive rates"),
                "interestRate", "10.5% onwards"
            ),
            Map.of(
                "name", "Home Loan",
                "description", "Make your dream home a reality",
                "features", List.of("Up to 30 year tenure", "Flexible EMI options", "No prepayment charges"),
                "interestRate", "8.5% onwards"
            ),
            Map.of(
                "name", "Vehicle Loan",
                "description", "Drive your dream vehicle today",
                "features", List.of("Up to 90% financing", "Quick processing", "Flexible repayment"),
                "interestRate", "9.5% onwards"
            )
        ));
        
        return ResponseEntity.ok(services);
    }

    /**
     * Get branch and ATM locations
     */
    @GetMapping("/locations")
    public ResponseEntity<Map<String, Object>> getLocations() {
        Map<String, Object> locations = new HashMap<>();
        
        locations.put("branches", List.of(
            Map.of(
                "name", "Main Branch",
                "address", "123 Banking Street, Financial District",
                "city", "Mumbai",
                "state", "Maharashtra",
                "pincode", "400001",
                "phone", "+91-22-12345678",
                "workingHours", "9:00 AM - 5:00 PM (Mon-Fri), 9:00 AM - 1:00 PM (Sat)",
                "services", List.of("All Banking Services", "Locker Facility", "Foreign Exchange")
            ),
            Map.of(
                "name", "City Center Branch",
                "address", "456 Commerce Plaza, Business District",
                "city", "Mumbai",
                "state", "Maharashtra",
                "pincode", "400002",
                "phone", "+91-22-87654321",
                "workingHours", "9:00 AM - 5:00 PM (Mon-Fri), 9:00 AM - 1:00 PM (Sat)",
                "services", List.of("Personal Banking", "Business Banking", "Investment Advisory")
            )
        ));
        
        locations.put("atms", List.of(
            Map.of(
                "location", "Shopping Mall - Level 1",
                "address", "789 Mall Road, Central Mumbai",
                "city", "Mumbai",
                "pincode", "400003",
                "services", List.of("Cash Withdrawal", "Balance Inquiry", "Fund Transfer", "Bill Payment"),
                "availability", "24x7"
            ),
            Map.of(
                "location", "Metro Station",
                "address", "Metro Station Complex, Platform 1",
                "city", "Mumbai", 
                "pincode", "400004",
                "services", List.of("Cash Withdrawal", "Balance Inquiry", "Quick Deposit"),
                "availability", "24x7"
            )
        ));
        
        return ResponseEntity.ok(locations);
    }

    /**
     * Get contact information
     */
    @GetMapping("/contact")
    public ResponseEntity<Map<String, Object>> getContactInfo() {
        Map<String, Object> contact = new HashMap<>();
        
        contact.put("customerCare", Map.of(
            "phone", "1800-XXX-XXXX",
            "email", "support@obs.com",
            "hours", "24x7",
            "languages", List.of("English", "Hindi", "Marathi", "Gujarati")
        ));
        
        contact.put("emergencyServices", Map.of(
            "cardBlock", "1800-YYY-YYYY",
            "fraudReporting", "1800-ZZZ-ZZZZ",
            "technicalSupport", "1800-AAA-AAAA"
        ));
        
        contact.put("headquarters", Map.of(
            "address", "OBS Tower, 100 Financial Street, BKC, Mumbai - 400051",
            "phone", "+91-22-12345678",
            "email", "info@obs.com"
        ));
        
        return ResponseEntity.ok(contact);
    }

    /**
     * Get frequently asked questions
     */
    @GetMapping("/faq")
    public ResponseEntity<Map<String, Object>> getFAQ() {
        Map<String, Object> faq = new HashMap<>();
        
        faq.put("account", List.of(
            Map.of(
                "question", "How do I open a savings account?",
                "answer", "You can open a savings account online through our website or by visiting any of our branches with required documents."
            ),
            Map.of(
                "question", "What is the minimum balance requirement?",
                "answer", "For savings accounts, the minimum balance requirement is ₹1,000. For current accounts, it's ₹5,000."
            ),
            Map.of(
                "question", "How do I activate my debit card?",
                "answer", "You can activate your debit card by visiting any ATM, calling customer care, or through online/mobile banking."
            )
        ));
        
        faq.put("digital", List.of(
            Map.of(
                "question", "How do I register for online banking?",
                "answer", "Visit our website, click on 'Register' and follow the steps using your account number and registered mobile number."
            ),
            Map.of(
                "question", "Is online banking secure?",
                "answer", "Yes, we use 256-bit SSL encryption and multi-factor authentication to ensure your transactions are secure."
            ),
            Map.of(
                "question", "What if I forget my login password?",
                "answer", "You can reset your password using the 'Forgot Password' option on the login page or call customer care."
            )
        ));
        
        return ResponseEntity.ok(faq);
    }

    /**
     * Get bank information and about us
     */
    @GetMapping("/about")
    public ResponseEntity<Map<String, Object>> getAboutInfo() {
        Map<String, Object> about = new HashMap<>();
        
        about.put("bankInfo", Map.of(
            "name", "Online Banking System (OBS)",
            "establishedYear", "2020",
            "headquarters", "Mumbai, Maharashtra",
            "type", "Digital Banking Platform",
            "license", "Banking License No. OBSB/2020/001"
        ));
        
        about.put("mission", "To provide secure, convenient, and innovative banking solutions that empower our customers to achieve their financial goals.");
        
        about.put("vision", "To be the leading digital banking platform, setting new standards in customer service and financial technology.");
        
        about.put("values", List.of(
            "Customer First",
            "Innovation & Technology",
            "Security & Trust",
            "Transparency",
            "Financial Inclusion"
        ));
        
        about.put("achievements", List.of(
            Map.of("year", "2023", "achievement", "Best Digital Banking Platform Award"),
            Map.of("year", "2022", "achievement", "Customer Service Excellence Award"),
            Map.of("year", "2021", "achievement", "Innovation in Banking Technology Award")
        ));
        
        return ResponseEntity.ok(about);
    }

    /**
     * Get current interest rates
     */
    @GetMapping("/rates")
    public ResponseEntity<Map<String, Object>> getInterestRates() {
        Map<String, Object> rates = new HashMap<>();
        
        rates.put("savingsAccount", Map.of(
            "rate", "3.5%",
            "compounding", "Quarterly",
            "effectiveFrom", "2025-01-01"
        ));
        
        rates.put("fixedDeposits", List.of(
            Map.of("tenure", "1-2 years", "rate", "6.5%"),
            Map.of("tenure", "2-3 years", "rate", "6.75%"),
            Map.of("tenure", "3-5 years", "rate", "7.0%"),
            Map.of("tenure", "5+ years", "rate", "7.25%")
        ));
        
        rates.put("loans", Map.of(
            "homeLoan", "8.5% onwards",
            "personalLoan", "10.5% onwards",
            "vehicleLoan", "9.5% onwards",
            "businessLoan", "11.0% onwards"
        ));
        
        rates.put("lastUpdated", "2025-08-29");
        
        return ResponseEntity.ok(rates);
    }

    /**
     * Submit contact form
     */
    @PostMapping("/contact")
    public ResponseEntity<Map<String, String>> submitContactForm(@RequestBody Map<String, String> contactForm) {
        // TODO: Implement contact form submission logic
        // This would typically save to database and send email notifications
        
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        response.put("message", "Thank you for contacting us. We will get back to you within 24 hours.");
        response.put("referenceId", "REF" + System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));
        health.put("service", "OBS Banking System");
        
        return ResponseEntity.ok(health);
    }
}
