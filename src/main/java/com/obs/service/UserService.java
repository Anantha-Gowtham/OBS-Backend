package com.obs.service;

import com.obs.model.*;
import com.obs.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanApplicationRepository loanRepository;
    private final UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private BillPaymentRepository billPaymentRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private StandingInstructionRepository standingInstructionRepository;

    public UserService(AccountRepository accountRepository,
                       TransactionRepository transactionRepository,
                       LoanApplicationRepository loanRepository,
                       UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    // Helper method to get current authenticated user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("No authenticated user found");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    // Basic banking operations - no authentication required for now
    public List<Account> getAllAccounts() { 
        return accountRepository.findAll(); 
    }
    
    public List<Transaction> getTransactions(String accountId, int page, int size) {
        return transactionRepository.findByAccountId(Long.parseLong(accountId), PageRequest.of(page, size)).getContent();
    }
    
    public Map<String, Object> transfer(Map<String, Object> data) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
            
            if (currentUser == null) {
                return Map.of("success", false, "message", "User not authenticated");
            }

            // Validate input parameters
            if (!data.containsKey("fromAccountId") || !data.containsKey("amount")) {
                return Map.of("success", false, "message", "Missing required parameters");
            }

            Long fromAccountId = Long.parseLong(data.get("fromAccountId").toString());
            BigDecimal amount = new BigDecimal(data.get("amount").toString());
            String transferType = (String) data.getOrDefault("transferType", "INTERNAL");
            String toAccountNumber = (String) data.get("toAccountNumber");
            String recipientName = (String) data.get("recipientName");
            String note = (String) data.getOrDefault("note", "Money Transfer");

            // Validate amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Map.of("success", false, "message", "Transfer amount must be greater than 0");
            }

            if (amount.compareTo(new BigDecimal("1000000")) > 0) {
                return Map.of("success", false, "message", "Transfer amount exceeds limit");
            }

            // Get sender account and verify ownership
            Optional<Account> fromAccountOpt = accountRepository.findById(fromAccountId);
            if (!fromAccountOpt.isPresent()) {
                return Map.of("success", false, "message", "Source account not found");
            }

            Account fromAccount = fromAccountOpt.get();
            
            // Verify account belongs to current user
            if (!fromAccount.getUser().getId().equals(currentUser.getId())) {
                return Map.of("success", false, "message", "Unauthorized access to account");
            }

            // Check account status
            if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
                return Map.of("success", false, "message", "Source account is not active");
            }

            // Check sufficient balance
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                return Map.of("success", false, "message", "Insufficient balance");
            }

            // Handle different transfer types
            Map<String, Object> result = new HashMap<>();
            String transactionId = generateTransactionId(transferType);

            switch (transferType.toUpperCase()) {
                case "INTERNAL":
                    result = processInternalTransfer(fromAccount, amount, toAccountNumber, recipientName, note, transactionId);
                    break;
                case "UPI":
                    result = processUpiTransfer(fromAccount, amount, toAccountNumber, recipientName, note, transactionId);
                    break;
                case "NEFT":
                    result = processNeftTransfer(fromAccount, amount, toAccountNumber, recipientName, note, transactionId);
                    break;
                case "RTGS":
                    result = processRtgsTransfer(fromAccount, amount, toAccountNumber, recipientName, note, transactionId);
                    break;
                default:
                    return Map.of("success", false, "message", "Invalid transfer type");
            }

            return result;

        } catch (Exception e) {
            return Map.of("success", false, "message", "Transfer failed: " + e.getMessage());
        }
    }

    private String generateTransactionId(String transferType) {
        String prefix = "";
        switch (transferType.toUpperCase()) {
            case "INTERNAL": prefix = "INT"; break;
            case "UPI": prefix = "UPI"; break;
            case "NEFT": prefix = "NEFT"; break;
            case "RTGS": prefix = "RTGS"; break;
            default: prefix = "TXN"; break;
        }
        return prefix + System.currentTimeMillis();
    }

    private Map<String, Object> processInternalTransfer(Account fromAccount, BigDecimal amount, 
            String toAccountNumber, String recipientName, String note, String transactionId) {
        try {
            // Find recipient account
            Optional<Account> toAccountOpt = accountRepository.findByAccountNumber(toAccountNumber);
            if (!toAccountOpt.isPresent()) {
                return Map.of("success", false, "message", "Recipient account not found");
            }

            Account toAccount = toAccountOpt.get();
            
            if (toAccount.getStatus() != AccountStatus.ACTIVE) {
                return Map.of("success", false, "message", "Recipient account is not active");
            }

            // Perform the transfer
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));

            // Save updated balances
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // Create debit transaction for sender
            Transaction debitTx = new Transaction();
            debitTx.setAccount(fromAccount);
            debitTx.setType(TransactionType.TRANSFER);
            debitTx.setAmount(amount.negate()); // Negative for debit
            debitTx.setNote("Transfer to " + toAccountNumber + " - " + note);
            debitTx.setStatus(TransactionStatus.COMPLETED);
            debitTx.setTransactionId(transactionId);
            debitTx.setRecipientAccount(toAccountNumber);
            debitTx.setRecipientName(recipientName);
            transactionRepository.save(debitTx);

            // Create credit transaction for recipient
            Transaction creditTx = new Transaction();
            creditTx.setAccount(toAccount);
            creditTx.setType(TransactionType.TRANSFER);
            creditTx.setAmount(amount); // Positive for credit
            creditTx.setNote("Transfer from " + fromAccount.getAccountNumber() + " - " + note);
            creditTx.setStatus(TransactionStatus.COMPLETED);
            creditTx.setTransactionId(transactionId);
            creditTx.setRecipientAccount(fromAccount.getAccountNumber());
            transactionRepository.save(creditTx);

            // Send real-time updates
            webSocketService.sendTransactionUpdate(Map.of(
                "transactionId", transactionId,
                "type", "INTERNAL_TRANSFER",
                "amount", amount.toString(),
                "fromAccount", fromAccount.getAccountNumber(),
                "toAccount", toAccount.getAccountNumber(),
                "status", "COMPLETED"
            ));

            // Send balance updates
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            webSocketService.sendBalanceUpdate(
                fromAccount.getAccountNumber(), 
                fromAccount.getBalance().toString(),
                auth.getName()
            );

            return Map.of(
                "success", true,
                "message", "Internal transfer completed successfully",
                "transactionId", transactionId,
                "amount", amount.toString(),
                "fromAccount", fromAccount.getAccountNumber(),
                "toAccount", toAccount.getAccountNumber(),
                "remainingBalance", fromAccount.getBalance().toString()
            );

        } catch (Exception e) {
            return Map.of("success", false, "message", "Internal transfer failed: " + e.getMessage());
        }
    }

    private Map<String, Object> processUpiTransfer(Account fromAccount, BigDecimal amount, 
            String upiId, String recipientName, String note, String transactionId) {
        try {
            // UPI transfers are external - just debit from source account
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            accountRepository.save(fromAccount);

            // Create transaction record
            Transaction tx = new Transaction();
            tx.setAccount(fromAccount);
            tx.setType(TransactionType.UPI);
            tx.setAmount(amount.negate());
            tx.setNote("UPI Transfer to " + upiId + " - " + note);
            tx.setStatus(TransactionStatus.COMPLETED);
            tx.setTransactionId(transactionId);
            tx.setRecipientAccount(upiId);
            tx.setRecipientName(recipientName);
            transactionRepository.save(tx);

            return Map.of(
                "success", true,
                "message", "UPI transfer completed successfully",
                "transactionId", transactionId,
                "amount", amount.toString(),
                "upiId", upiId,
                "remainingBalance", fromAccount.getBalance().toString()
            );

        } catch (Exception e) {
            return Map.of("success", false, "message", "UPI transfer failed: " + e.getMessage());
        }
    }

    private Map<String, Object> processNeftTransfer(Account fromAccount, BigDecimal amount, 
            String toAccountNumber, String recipientName, String note, String transactionId) {
        try {
            // NEFT minimum amount validation
            if (amount.compareTo(new BigDecimal("1")) < 0) {
                return Map.of("success", false, "message", "NEFT minimum amount is ₹1");
            }

            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            accountRepository.save(fromAccount);

            Transaction tx = new Transaction();
            tx.setAccount(fromAccount);
            tx.setType(TransactionType.NEFT);
            tx.setAmount(amount.negate());
            tx.setNote("NEFT Transfer to " + toAccountNumber + " - " + note);
            tx.setStatus(TransactionStatus.COMPLETED);
            tx.setTransactionId(transactionId);
            tx.setRecipientAccount(toAccountNumber);
            tx.setRecipientName(recipientName);
            transactionRepository.save(tx);

            return Map.of(
                "success", true,
                "message", "NEFT transfer completed successfully",
                "transactionId", transactionId,
                "amount", amount.toString(),
                "toAccount", toAccountNumber,
                "remainingBalance", fromAccount.getBalance().toString()
            );

        } catch (Exception e) {
            return Map.of("success", false, "message", "NEFT transfer failed: " + e.getMessage());
        }
    }

    private Map<String, Object> processRtgsTransfer(Account fromAccount, BigDecimal amount, 
            String toAccountNumber, String recipientName, String note, String transactionId) {
        try {
            // RTGS minimum amount validation
            if (amount.compareTo(new BigDecimal("200000")) < 0) {
                return Map.of("success", false, "message", "RTGS minimum amount is ₹2,00,000");
            }

            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            accountRepository.save(fromAccount);

            Transaction tx = new Transaction();
            tx.setAccount(fromAccount);
            tx.setType(TransactionType.RTGS);
            tx.setAmount(amount.negate());
            tx.setNote("RTGS Transfer to " + toAccountNumber + " - " + note);
            tx.setStatus(TransactionStatus.COMPLETED);
            tx.setTransactionId(transactionId);
            tx.setRecipientAccount(toAccountNumber);
            tx.setRecipientName(recipientName);
            transactionRepository.save(tx);

            return Map.of(
                "success", true,
                "message", "RTGS transfer completed successfully",
                "transactionId", transactionId,
                "amount", amount.toString(),
                "toAccount", toAccountNumber,
                "remainingBalance", fromAccount.getBalance().toString()
            );

        } catch (Exception e) {
            return Map.of("success", false, "message", "RTGS transfer failed: " + e.getMessage());
        }
    }
    
    public Map<String, Object> applyLoan(Map<String, Object> data) {
        // Simplified loan application without user authentication
        LoanApplication loan = new LoanApplication();
        loan.setAmount(new BigDecimal(data.get("amount").toString()));
        loanRepository.save(loan);
        return Map.of("loanId", loan.getId(), "status", loan.getStatus().name());
    }
    
    public List<LoanApplication> getAllLoans() { 
        return loanRepository.findAll(); 
    }
    
    public Map<String, Object> getAccountBalance(String accountId) {
        Long id = Long.parseLong(accountId);
        Account account = accountRepository.findById(id).orElseThrow();
        
        return Map.of(
            "accountId", accountId,
            "availableBalance", account.getBalance(),
            "totalBalance", account.getBalance(),
            "clearedBalance", account.getBalance().multiply(new BigDecimal("0.95")),
            "pendingAmount", account.getBalance().multiply(new BigDecimal("0.05")),
            "lastUpdated", LocalDateTime.now().toString()
        );
    }

    public Map<String, Object> getAccountDetails(String accountId) {
        Long id = Long.parseLong(accountId);
        Account account = accountRepository.findById(id).orElseThrow();
        
        return Map.of(
            "accountId", account.getId(),
            "accountNumber", account.getAccountNumber(),
            "accountType", account.getAccountType(),
            "balance", account.getBalance(),
            "status", account.getStatus().name(),
            "createdAt", account.getCreatedAt().toString()
        );
    }

    // Mock data generators for demo purposes
    private List<Map<String, Object>> generateMockBeneficiaries() {
        return Arrays.asList(
            Map.of("id", "BEN001", "name", "John Doe", "accountNumber", "1234567890", "ifsc", "SBI0001234"),
            Map.of("id", "BEN002", "name", "Jane Smith", "accountNumber", "0987654321", "ifsc", "HDFC0005678")
        );
    }

    public List<Map<String, Object>> getBeneficiaries() {
        try {
            User currentUser = getCurrentUser();
            List<Beneficiary> beneficiaries = beneficiaryRepository.findByUserIdAndIsActiveTrue(currentUser.getId());
            
            return beneficiaries.stream().map(beneficiary -> {
                Map<String, Object> beneficiaryMap = new HashMap<>();
                beneficiaryMap.put("id", beneficiary.getId());
                beneficiaryMap.put("name", beneficiary.getBeneficiaryName());
                beneficiaryMap.put("accountNumber", beneficiary.getAccountNumber());
                beneficiaryMap.put("ifscCode", beneficiary.getIfscCode());
                beneficiaryMap.put("bankName", beneficiary.getBankName());
                beneficiaryMap.put("nickname", beneficiary.getNickname());
                beneficiaryMap.put("type", beneficiary.getBeneficiaryType().name());
                beneficiaryMap.put("verified", beneficiary.isVerified());
                beneficiaryMap.put("lastUsed", beneficiary.getLastUsed() != null ? 
                    beneficiary.getLastUsed().toString() : null);
                beneficiaryMap.put("createdAt", beneficiary.getCreatedAt().toString());
                return beneficiaryMap;
            }).collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve beneficiaries: " + e.getMessage());
        }
    }

    public Map<String, Object> addBeneficiary(Map<String, Object> data) {
        try {
            User currentUser = getCurrentUser();
            
            String accountNumber = (String) data.get("accountNumber");
            String beneficiaryName = (String) data.get("name");
            String ifscCode = (String) data.get("ifscCode");
            String bankName = (String) data.get("bankName");
            String nickname = (String) data.get("nickname");
            String typeStr = (String) data.get("type");
            
            // Validate required fields
            if (accountNumber == null || beneficiaryName == null || ifscCode == null || bankName == null) {
                throw new RuntimeException("Missing required beneficiary information");
            }
            
            // Check if beneficiary already exists
            if (beneficiaryRepository.existsByAccountNumberAndUserId(accountNumber, currentUser.getId())) {
                throw new RuntimeException("Beneficiary with this account number already exists");
            }
            
            // Parse beneficiary type
            BeneficiaryType beneficiaryType;
            try {
                beneficiaryType = BeneficiaryType.valueOf(typeStr.toUpperCase());
            } catch (Exception e) {
                beneficiaryType = BeneficiaryType.EXTERNAL; // Default
            }
            
            // Create new beneficiary
            Beneficiary beneficiary = new Beneficiary(
                currentUser.getId(), 
                beneficiaryName, 
                accountNumber, 
                ifscCode, 
                bankName, 
                beneficiaryType
            );
            
            if (nickname != null && !nickname.trim().isEmpty()) {
                beneficiary.setNickname(nickname.trim());
            }
            
            // For internal accounts, mark as verified automatically
            if (beneficiaryType == BeneficiaryType.INTERNAL) {
                // Check if internal account exists
                Optional<Account> internalAccount = accountRepository.findByAccountNumber(accountNumber);
                if (internalAccount.isPresent()) {
                    beneficiary.setVerified(true);
                } else {
                    throw new RuntimeException("Internal account not found");
                }
            }
            
            Beneficiary savedBeneficiary = beneficiaryRepository.save(beneficiary);
            
            // Send real-time update
            webSocketService.sendUserUpdate(currentUser.getId(), "beneficiary_added", 
                Map.of("beneficiaryId", savedBeneficiary.getId(), "name", beneficiaryName));
            
            return Map.of(
                "success", true,
                "message", "Beneficiary added successfully",
                "beneficiaryId", savedBeneficiary.getId(),
                "name", beneficiaryName,
                "accountNumber", accountNumber,
                "verified", savedBeneficiary.isVerified()
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to add beneficiary: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> deleteBeneficiary(String beneficiaryId) {
        try {
            User currentUser = getCurrentUser();
            Long id = Long.parseLong(beneficiaryId);
            
            Optional<Beneficiary> beneficiaryOpt = beneficiaryRepository.findById(id);
            if (beneficiaryOpt.isEmpty()) {
                throw new RuntimeException("Beneficiary not found");
            }
            
            Beneficiary beneficiary = beneficiaryOpt.get();
            
            // Check ownership
            if (!beneficiary.getUserId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized to delete this beneficiary");
            }
            
            // Soft delete - mark as inactive
            beneficiary.setActive(false);
            beneficiaryRepository.save(beneficiary);
            
            // Send real-time update
            webSocketService.sendUserUpdate(currentUser.getId(), "beneficiary_deleted", 
                Map.of("beneficiaryId", id));
            
            return Map.of(
                "success", true,
                "message", "Beneficiary deleted successfully",
                "beneficiaryId", beneficiaryId
            );
        } catch (NumberFormatException e) {
            return Map.of(
                "success", false,
                "message", "Invalid beneficiary ID"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to delete beneficiary: " + e.getMessage()
            );
        }
    }
    
    // Authentication and Profile methods
    public List<Map<String, Object>> accounts() {
        List<Account> accounts = getAllAccounts();
        return accounts.stream().map(account -> Map.<String, Object>of(
            "id", account.getId(),
            "accountNumber", account.getAccountNumber(),
            "type", account.getAccountType(),
            "balance", account.getBalance(),
            "status", account.getStatus().toString()
        )).toList();
    }
    
    public List<Map<String, Object>> transactions(String accountId, int page, int size) {
        List<Transaction> transactions = getTransactions(accountId, page, size);
        return transactions.stream().map(tx -> Map.<String, Object>of(
            "id", tx.getId(),
            "amount", tx.getAmount(),
            "type", tx.getType().toString(),
            "status", tx.getStatus().toString(),
            "note", tx.getNote() != null ? tx.getNote() : ""
        )).toList();
    }
    
    // Account Statement System - Real Implementation
    public Map<String, Object> getAccountStatements(String accountId, String fromDate, String toDate) {
        try {
            User currentUser = getCurrentUser();
            
            // Validate account ownership
            Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountId);
            if (accountOpt.isEmpty()) {
                throw new RuntimeException("Account not found");
            }
            
            Account account = accountOpt.get();
            if (!account.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized access to account");
            }
            
            // Parse date range
            LocalDateTime tempStartDate = LocalDateTime.now().minusMonths(3); // Default 3 months
            LocalDateTime tempEndDate = LocalDateTime.now();
            
            if (fromDate != null && !fromDate.trim().isEmpty()) {
                try {
                    tempStartDate = LocalDateTime.parse(fromDate + "T00:00:00");
                } catch (Exception e) {
                    // Use default if parsing fails
                }
            }
            
            if (toDate != null && !toDate.trim().isEmpty()) {
                try {
                    tempEndDate = LocalDateTime.parse(toDate + "T23:59:59");
                } catch (Exception e) {
                    tempEndDate = LocalDateTime.now();
                }
            }
            
            final LocalDateTime startDate = tempStartDate;
            final LocalDateTime endDate = tempEndDate;
            
            // Get transactions for the account
            List<Transaction> transactions = transactionRepository.findByAccountId(account.getId(), 
                PageRequest.of(0, 1000)).getContent(); // Get up to 1000 transactions
            
            // Filter by date range
            List<Transaction> filteredTransactions = transactions.stream()
                .filter(t -> {
                    LocalDateTime txnDate = t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                    return txnDate.isAfter(startDate) && txnDate.isBefore(endDate);
                })
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt())) // Latest first
                .collect(java.util.stream.Collectors.toList());
            
            // Calculate summary statistics
            BigDecimal totalCredits = filteredTransactions.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT || 
                           t.getType() == TransactionType.TRANSFER)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalDebits = filteredTransactions.stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAWAL || 
                           t.getType() == TransactionType.UPI ||
                           t.getType() == TransactionType.NEFT ||
                           t.getType() == TransactionType.RTGS ||
                           t.getType() == TransactionType.PAYMENT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Prepare statement data
            List<Map<String, Object>> transactionList = filteredTransactions.stream()
                .map(transaction -> {
                    Map<String, Object> txnMap = new HashMap<>();
                    txnMap.put("id", transaction.getId());
                    txnMap.put("transactionId", transaction.getTransactionId());
                    txnMap.put("date", transaction.getCreatedAt().toString());
                    txnMap.put("type", transaction.getType().name());
                    txnMap.put("amount", transaction.getAmount());
                    txnMap.put("description", transaction.getNote());
                    txnMap.put("status", transaction.getStatus().name());
                    txnMap.put("recipientAccount", transaction.getRecipientAccount());
                    txnMap.put("recipientName", transaction.getRecipientName());
                    
                    // Determine debit/credit
                    boolean isCredit = transaction.getType() == TransactionType.DEPOSIT || 
                                     transaction.getType() == TransactionType.TRANSFER;
                    txnMap.put("creditAmount", isCredit ? transaction.getAmount() : null);
                    txnMap.put("debitAmount", !isCredit ? transaction.getAmount() : null);
                    
                    return txnMap;
                }).collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> statement = new HashMap<>();
            statement.put("accountNumber", account.getAccountNumber());
            statement.put("accountHolderName", currentUser.getUsername());
            statement.put("accountType", account.getAccountType());
            statement.put("currentBalance", account.getBalance());
            statement.put("statementPeriod", Map.of(
                "from", startDate.toLocalDate().toString(),
                "to", endDate.toLocalDate().toString()
            ));
            statement.put("summary", Map.of(
                "totalCredits", totalCredits,
                "totalDebits", totalDebits,
                "netAmount", totalCredits.subtract(totalDebits),
                "transactionCount", filteredTransactions.size()
            ));
            statement.put("transactions", transactionList);
            statement.put("generatedAt", LocalDateTime.now().toString());
            
            return Map.of(
                "success", true,
                "statement", statement
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to generate statement: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> downloadStatement(String accountId, Map<String, String> params) {
        try {
            String format = params.getOrDefault("format", "PDF").toUpperCase();
            String fromDate = params.get("fromDate");
            String toDate = params.get("toDate");
            
            // Get statement data
            Map<String, Object> statementResponse = getAccountStatements(accountId, fromDate, toDate);
            
            if (!(Boolean) statementResponse.get("success")) {
                return statementResponse; // Return error if statement generation failed
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> statement = (Map<String, Object>) statementResponse.get("statement");
            
            // Generate download URL/reference (in real implementation, this would generate actual file)
            String downloadId = "STMT_" + System.currentTimeMillis();
            String fileName = "statement_" + accountId + "_" + 
                            LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                            "." + format.toLowerCase();
            
            // In a real implementation, you would:
            // 1. Generate PDF/Excel file using libraries like iText or Apache POI
            // 2. Store file temporarily or in cloud storage
            // 3. Return download URL
            
            return Map.of(
                "success", true,
                "message", "Statement prepared for download",
                "downloadId", downloadId,
                "fileName", fileName,
                "format", format,
                "downloadUrl", "/api/statements/download/" + downloadId,
                "expiresAt", LocalDateTime.now().plusHours(24).toString(), // 24-hour expiry
                "statement", statement // Include statement data for immediate viewing
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to prepare statement download: " + e.getMessage()
            );
        }
    }
    
    public Map<String, Object> internalTransfer(Map<String, Object> data) {
        return Map.of("message", "Internal transfer completed", "transactionId", "TXN" + System.currentTimeMillis());
    }
    
    public Map<String, Object> externalTransfer(Map<String, Object> data) {
        return Map.of("message", "External transfer completed", "transactionId", "TXN" + System.currentTimeMillis());
    }
    
    public Map<String, Object> upiTransfer(Map<String, Object> data) {
        return Map.of("message", "UPI transfer completed", "transactionId", "UPI" + System.currentTimeMillis());
    }
    
    public Map<String, Object> neftTransfer(Map<String, Object> data) {
        return Map.of("message", "NEFT transfer completed", "transactionId", "NEFT" + System.currentTimeMillis());
    }
    
    public Map<String, Object> rtgsTransfer(Map<String, Object> data) {
        return Map.of("message", "RTGS transfer completed", "transactionId", "RTGS" + System.currentTimeMillis());
    }
    
    public List<Map<String, Object>> getBillPayments() {
        try {
            User currentUser = getCurrentUser();
            List<BillPayment> billPayments = billPaymentRepository.findRecentBillPayments(currentUser.getId());
            
            return billPayments.stream()
                    .limit(50) // Limit to recent 50 payments
                    .map(payment -> {
                        Map<String, Object> paymentMap = new HashMap<>();
                        paymentMap.put("id", payment.getId());
                        paymentMap.put("paymentId", payment.getPaymentId());
                        paymentMap.put("billType", payment.getBillType().name());
                        paymentMap.put("provider", payment.getBillerName());
                        paymentMap.put("consumerNumber", payment.getConsumerNumber());
                        paymentMap.put("consumerName", payment.getConsumerName());
                        paymentMap.put("amount", payment.getAmount());
                        paymentMap.put("dueAmount", payment.getDueAmount());
                        paymentMap.put("dueDate", payment.getDueDate() != null ? 
                            payment.getDueDate().toString() : null);
                        paymentMap.put("status", payment.getStatus().name());
                        paymentMap.put("paymentDate", payment.getPaymentDate().toString());
                        paymentMap.put("transactionId", payment.getTransactionId());
                        paymentMap.put("description", payment.getDescription());
                        return paymentMap;
                    }).collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve bill payments: " + e.getMessage());
        }
    }

    public Map<String, Object> payBill(Map<String, Object> data) {
        try {
            User currentUser = getCurrentUser();
            
            String billTypeStr = (String) data.get("billType");
            String billerName = (String) data.get("provider");
            String consumerNumber = (String) data.get("consumerNumber");
            String consumerName = (String) data.get("consumerName");
            Object amountObj = data.get("amount");
            String accountNumber = (String) data.get("fromAccount");
            String description = (String) data.get("description");
            
            // Validate required fields
            if (billTypeStr == null || billerName == null || consumerNumber == null || amountObj == null) {
                throw new RuntimeException("Missing required bill payment information");
            }
            
            BigDecimal amount = new BigDecimal(amountObj.toString());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Invalid payment amount");
            }
            
            // Parse bill type
            BillType billType;
            try {
                billType = BillType.valueOf(billTypeStr.toUpperCase());
            } catch (Exception e) {
                billType = BillType.OTHER;
            }
            
            // Get user account for balance check
            Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
            if (accountOpt.isEmpty()) {
                throw new RuntimeException("Source account not found");
            }
            
            Account account = accountOpt.get();
            if (!account.getUserId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized access to account");
            }
            
            if (account.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("Insufficient funds");
            }
            
            // Create bill payment record
            BillPayment billPayment = new BillPayment(
                currentUser.getId(), 
                billType, 
                billerName, 
                consumerNumber, 
                consumerName != null ? consumerName : "Not Provided", 
                amount
            );
            
            billPayment.setAccountNumber(accountNumber);
            if (description != null) {
                billPayment.setDescription(description);
            }
            
            // Save bill payment
            BillPayment savedPayment = billPaymentRepository.save(billPayment);
            
            // Deduct amount from account
            account.setBalance(account.getBalance().subtract(amount));
            accountRepository.save(account);
            
            // Create transaction record
            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setType(TransactionType.PAYMENT);
            transaction.setAmount(amount);
            transaction.setNote("Bill Payment - " + billerName + " (" + consumerNumber + ")");
            transaction.setTransactionId(savedPayment.getPaymentId());
            transaction.setStatus(TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            // Update bill payment with transaction ID
            savedPayment.markAsCompleted(savedTransaction.getTransactionId());
            billPaymentRepository.save(savedPayment);
            
            // Send real-time updates
            webSocketService.sendUserUpdate(currentUser.getId(), "bill_paid", 
                Map.of("paymentId", savedPayment.getPaymentId(), "amount", amount, "provider", billerName));
            webSocketService.sendBalanceUpdate(account.getUserId(), account.getAccountNumber(), account.getBalance());
            
            return Map.of(
                "success", true,
                "message", "Bill payment completed successfully",
                "paymentId", savedPayment.getPaymentId(),
                "transactionId", savedTransaction.getTransactionId(),
                "amount", amount,
                "newBalance", account.getBalance()
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Bill payment failed: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> mobileRecharge(Map<String, Object> data) {
        try {
            // Mobile recharge is essentially a bill payment with MOBILE type
            data.put("billType", "MOBILE");
            data.put("provider", data.getOrDefault("operator", "Mobile Operator"));
            data.put("consumerNumber", data.get("mobileNumber"));
            data.put("consumerName", "Mobile Recharge");
            data.put("description", "Mobile Recharge for " + data.get("mobileNumber"));
            
            return payBill(data);
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Mobile recharge failed: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> utilityPayment(Map<String, Object> data) {
        try {
            // Utility payment is a generic bill payment
            String utilityType = (String) data.getOrDefault("utilityType", "OTHER");
            data.put("billType", utilityType.toUpperCase());
            
            return payBill(data);
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Utility payment failed: " + e.getMessage()
            );
        }
    }
    
    public List<Map<String, Object>> getStandingInstructions() {
        try {
            User currentUser = getCurrentUser();
            List<StandingInstruction> instructions = standingInstructionRepository.findByUserId(currentUser.getId());
            
            return instructions.stream().map(instruction -> {
                Map<String, Object> instructionMap = new HashMap<>();
                instructionMap.put("id", instruction.getId());
                instructionMap.put("instructionId", instruction.getInstructionId());
                instructionMap.put("instructionName", instruction.getInstructionName());
                instructionMap.put("description", instruction.getDescription());
                instructionMap.put("instructionType", instruction.getInstructionType().getDisplayName());
                instructionMap.put("fromAccount", instruction.getFromAccount());
                instructionMap.put("toAccount", instruction.getToAccount());
                instructionMap.put("beneficiaryName", instruction.getBeneficiaryName());
                instructionMap.put("amount", instruction.getAmount());
                instructionMap.put("frequency", instruction.getFrequency().getDisplayName());
                instructionMap.put("startDate", instruction.getStartDate().toString());
                instructionMap.put("endDate", instruction.getEndDate() != null ? instruction.getEndDate().toString() : null);
                instructionMap.put("nextExecutionDate", instruction.getNextExecutionDate().toString());
                instructionMap.put("lastExecuted", instruction.getLastExecuted() != null ? instruction.getLastExecuted().toString() : null);
                instructionMap.put("status", instruction.getStatus().getDisplayName());
                instructionMap.put("executionCount", instruction.getExecutionCount());
                instructionMap.put("maxExecutions", instruction.getMaxExecutions());
                instructionMap.put("isDue", instruction.isDue());
                instructionMap.put("isActive", instruction.isActive());
                instructionMap.put("createdAt", instruction.getCreatedAt().toString());
                instructionMap.put("updatedAt", instruction.getUpdatedAt() != null ? instruction.getUpdatedAt().toString() : null);
                return instructionMap;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            logger.error("Error fetching standing instructions: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> createStandingInstruction(Map<String, Object> data) {
        try {
            User currentUser = getCurrentUser();
            
            // Extract and validate data
            String instructionName = (String) data.get("instructionName");
            String description = (String) data.get("description");
            InstructionType instructionType = InstructionType.valueOf((String) data.get("instructionType"));
            String fromAccount = (String) data.get("fromAccount");
            String toAccount = (String) data.get("toAccount");
            String beneficiaryName = (String) data.get("beneficiaryName");
            BigDecimal amount = new BigDecimal(data.get("amount").toString());
            InstructionFrequency frequency = InstructionFrequency.valueOf((String) data.get("frequency"));
            LocalDate startDate = LocalDate.parse((String) data.get("startDate"));
            LocalDate endDate = data.get("endDate") != null ? LocalDate.parse((String) data.get("endDate")) : null;
            Integer maxExecutions = data.get("maxExecutions") != null ? Integer.parseInt(data.get("maxExecutions").toString()) : null;
            
            // Validate account ownership
            Optional<Account> accountOpt = accountRepository.findByAccountNumber(fromAccount);
            if (accountOpt.isEmpty() || !accountOpt.get().getUserId().equals(currentUser.getId())) {
                return Map.of("success", false, "message", "Invalid from account");
            }
            Account account = accountOpt.get();
            
            // Validate amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Map.of("success", false, "message", "Amount must be greater than zero");
            }
            
            // Validate dates
            if (endDate != null && endDate.isBefore(startDate)) {
                return Map.of("success", false, "message", "End date must be after start date");
            }
            
            // Create standing instruction
            StandingInstruction instruction = new StandingInstruction(
                currentUser.getId(),
                instructionName,
                instructionType,
                fromAccount,
                toAccount,
                amount,
                frequency,
                startDate
            );
            
            instruction.setDescription(description);
            instruction.setBeneficiaryName(beneficiaryName);
            instruction.setEndDate(endDate);
            instruction.setMaxExecutions(maxExecutions);
            
            StandingInstruction saved = standingInstructionRepository.save(instruction);
            
            // Send WebSocket notification
            webSocketService.sendUserNotification(currentUser.getUsername(), 
                "Standing instruction '" + instructionName + "' created successfully", "SUCCESS");
            
            return Map.of(
                "success", true,
                "message", "Standing instruction created successfully",
                "instructionId", saved.getInstructionId(),
                "nextExecutionDate", saved.getNextExecutionDate().toString()
            );
            
        } catch (Exception e) {
            logger.error("Error creating standing instruction: " + e.getMessage(), e);
            return Map.of("success", false, "message", "Error creating standing instruction: " + e.getMessage());
        }
    }
    
    public Map<String, Object> updateStandingInstruction(String instructionId, Map<String, Object> data) {
        try {
            User currentUser = getCurrentUser();
            
            Optional<StandingInstruction> instructionOpt = standingInstructionRepository
                .findByInstructionIdAndUserId(instructionId, currentUser.getId());
            
            if (instructionOpt.isEmpty()) {
                return Map.of("success", false, "message", "Standing instruction not found");
            }
            
            StandingInstruction instruction = instructionOpt.get();
            
            // Update fields if provided
            if (data.containsKey("instructionName")) {
                instruction.setInstructionName((String) data.get("instructionName"));
            }
            if (data.containsKey("description")) {
                instruction.setDescription((String) data.get("description"));
            }
            if (data.containsKey("amount")) {
                BigDecimal newAmount = new BigDecimal(data.get("amount").toString());
                if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    return Map.of("success", false, "message", "Amount must be greater than zero");
                }
                instruction.setAmount(newAmount);
            }
            if (data.containsKey("frequency")) {
                InstructionFrequency newFrequency = InstructionFrequency.valueOf((String) data.get("frequency"));
                instruction.setFrequency(newFrequency);
            }
            if (data.containsKey("endDate")) {
                LocalDate endDate = data.get("endDate") != null ? LocalDate.parse((String) data.get("endDate")) : null;
                if (endDate != null && endDate.isBefore(instruction.getStartDate())) {
                    return Map.of("success", false, "message", "End date must be after start date");
                }
                instruction.setEndDate(endDate);
            }
            if (data.containsKey("maxExecutions")) {
                Integer maxExecutions = data.get("maxExecutions") != null ? 
                    Integer.parseInt(data.get("maxExecutions").toString()) : null;
                instruction.setMaxExecutions(maxExecutions);
            }
            
            standingInstructionRepository.save(instruction);
            
            // Send WebSocket notification
            webSocketService.sendUserNotification(currentUser.getUsername(), 
                "Standing instruction '" + instruction.getInstructionName() + "' updated successfully", "SUCCESS");
            
            return Map.of(
                "success", true,
                "message", "Standing instruction updated successfully",
                "instructionId", instruction.getInstructionId()
            );
            
        } catch (Exception e) {
            logger.error("Error updating standing instruction: " + e.getMessage(), e);
            return Map.of("success", false, "message", "Error updating standing instruction: " + e.getMessage());
        }
    }
    
    public Map<String, Object> pauseStandingInstruction(String instructionId) {
        try {
            User currentUser = getCurrentUser();
            
            Optional<StandingInstruction> instructionOpt = standingInstructionRepository
                .findByInstructionIdAndUserId(instructionId, currentUser.getId());
            
            if (instructionOpt.isEmpty()) {
                return Map.of("success", false, "message", "Standing instruction not found");
            }
            
            StandingInstruction instruction = instructionOpt.get();
            
            if (!instruction.isActive()) {
                return Map.of("success", false, "message", "Instruction is not active");
            }
            
            instruction.pauseInstruction();
            standingInstructionRepository.save(instruction);
            
            // Send WebSocket notification
            webSocketService.sendUserNotification(currentUser.getUsername(), 
                "Standing instruction '" + instruction.getInstructionName() + "' paused", "INFO");
            
            return Map.of(
                "success", true,
                "message", "Standing instruction paused successfully",
                "instructionId", instructionId
            );
            
        } catch (Exception e) {
            logger.error("Error pausing standing instruction: " + e.getMessage(), e);
            return Map.of("success", false, "message", "Error pausing standing instruction: " + e.getMessage());
        }
    }
    
    public Map<String, Object> resumeStandingInstruction(String instructionId) {
        try {
            User currentUser = getCurrentUser();
            
            Optional<StandingInstruction> instructionOpt = standingInstructionRepository
                .findByInstructionIdAndUserId(instructionId, currentUser.getId());
            
            if (instructionOpt.isEmpty()) {
                return Map.of("success", false, "message", "Standing instruction not found");
            }
            
            StandingInstruction instruction = instructionOpt.get();
            
            if (instruction.getStatus() != InstructionStatus.PAUSED) {
                return Map.of("success", false, "message", "Instruction is not paused");
            }
            
            instruction.resumeInstruction();
            standingInstructionRepository.save(instruction);
            
            // Send WebSocket notification
            webSocketService.sendUserNotification(currentUser.getUsername(), 
                "Standing instruction '" + instruction.getInstructionName() + "' resumed", "INFO");
            
            return Map.of(
                "success", true,
                "message", "Standing instruction resumed successfully",
                "instructionId", instructionId
            );
            
        } catch (Exception e) {
            logger.error("Error resuming standing instruction: " + e.getMessage(), e);
            return Map.of("success", false, "message", "Error resuming standing instruction: " + e.getMessage());
        }
    }

    public Map<String, Object> cancelStandingInstruction(String instructionId) {
        try {
            User currentUser = getCurrentUser();
            
            Optional<StandingInstruction> instructionOpt = standingInstructionRepository
                .findByInstructionIdAndUserId(instructionId, currentUser.getId());
            
            if (instructionOpt.isEmpty()) {
                return Map.of("success", false, "message", "Standing instruction not found");
            }
            
            StandingInstruction instruction = instructionOpt.get();
            
            instruction.cancelInstruction("Cancelled by user");
            standingInstructionRepository.save(instruction);
            
            // Send WebSocket notification
            webSocketService.sendUserNotification(currentUser.getUsername(), 
                "Standing instruction '" + instruction.getInstructionName() + "' cancelled", "WARNING");
            
            return Map.of(
                "success", true,
                "message", "Standing instruction cancelled successfully",
                "instructionId", instructionId
            );
            
        } catch (Exception e) {
            logger.error("Error cancelling standing instruction: " + e.getMessage(), e);
            return Map.of("success", false, "message", "Error cancelling standing instruction: " + e.getMessage());
        }
    }
    
    public List<Map<String, Object>> getDueInstructions() {
        try {
            User currentUser = getCurrentUser();
            List<StandingInstruction> dueInstructions = standingInstructionRepository
                .findDueInstructionsByUser(currentUser.getId(), LocalDate.now());
            
            return dueInstructions.stream().map(instruction -> {
                Map<String, Object> instructionMap = new HashMap<>();
                instructionMap.put("instructionId", instruction.getInstructionId());
                instructionMap.put("instructionName", instruction.getInstructionName());
                instructionMap.put("amount", instruction.getAmount());
                instructionMap.put("toAccount", instruction.getToAccount());
                instructionMap.put("beneficiaryName", instruction.getBeneficiaryName());
                instructionMap.put("nextExecutionDate", instruction.getNextExecutionDate().toString());
                return instructionMap;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            logger.error("Error fetching due instructions: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    public Map<String, Object> getInstructionStatistics() {
        try {
            User currentUser = getCurrentUser();
            
            List<StandingInstruction> allInstructions = standingInstructionRepository.findByUserId(currentUser.getId());
            long totalInstructions = allInstructions.size();
            long activeInstructions = standingInstructionRepository.countByUserIdAndStatus(currentUser.getId(), InstructionStatus.ACTIVE);
            long pausedInstructions = standingInstructionRepository.countByUserIdAndStatus(currentUser.getId(), InstructionStatus.PAUSED);
            long completedInstructions = standingInstructionRepository.countByUserIdAndStatus(currentUser.getId(), InstructionStatus.COMPLETED);
            
            List<StandingInstruction> dueToday = standingInstructionRepository
                .findDueInstructionsByUser(currentUser.getId(), LocalDate.now());
            
            List<StandingInstruction> expiringSoon = standingInstructionRepository
                .findExpiringInstructions(currentUser.getId(), LocalDate.now(), LocalDate.now().plusDays(30));
            
            return Map.of(
                "totalInstructions", totalInstructions,
                "activeInstructions", activeInstructions,
                "pausedInstructions", pausedInstructions,
                "completedInstructions", completedInstructions,
                "dueToday", dueToday.size(),
                "expiringSoon", expiringSoon.size()
            );
            
        } catch (Exception e) {
            logger.error("Error fetching instruction statistics: " + e.getMessage(), e);
            return Map.of("error", "Unable to fetch statistics");
        }
    }
    
    public List<Map<String, Object>> getCards() {
        try {
            User currentUser = getCurrentUser();
            List<Card> cards = cardRepository.findByUserId(currentUser.getId());
            
            return cards.stream().map(card -> {
                Map<String, Object> cardMap = new HashMap<>();
                cardMap.put("id", card.getId());
                cardMap.put("cardNumber", card.getMaskedCardNumber());
                cardMap.put("last4Digits", card.getLast4Digits());
                cardMap.put("cardHolderName", card.getCardHolderName());
                cardMap.put("type", card.getCardType().name());
                cardMap.put("status", card.getStatus().name());
                cardMap.put("expiryDate", card.getExpiryDate().toString());
                cardMap.put("dailyLimit", card.getDailyLimit());
                cardMap.put("monthlyLimit", card.getMonthlyLimit());
                cardMap.put("contactlessEnabled", card.isContactlessEnabled());
                cardMap.put("onlineTransactionEnabled", card.isOnlineTransactionEnabled());
                cardMap.put("internationalUsageEnabled", card.isInternationalUsageEnabled());
                cardMap.put("lastUsed", card.getLastUsed() != null ? card.getLastUsed().toString() : null);
                cardMap.put("createdAt", card.getCreatedAt().toString());
                cardMap.put("accountNumber", card.getAccount().getAccountNumber());
                cardMap.put("blockReason", card.getBlockReason());
                return cardMap;
            }).collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve cards: " + e.getMessage());
        }
    }

    public Map<String, Object> getCardDetails(String cardId) {
        try {
            User currentUser = getCurrentUser();
            Long id = Long.parseLong(cardId);
            
            Optional<Card> cardOpt = cardRepository.findById(id);
            if (cardOpt.isEmpty()) {
                throw new RuntimeException("Card not found");
            }
            
            Card card = cardOpt.get();
            
            // Check ownership
            if (!card.getAccount().getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized access to card");
            }
            
            Map<String, Object> cardDetails = new HashMap<>();
            cardDetails.put("id", card.getId());
            cardDetails.put("cardNumber", card.getMaskedCardNumber());
            cardDetails.put("cardHolderName", card.getCardHolderName());
            cardDetails.put("type", card.getCardType().name());
            cardDetails.put("status", card.getStatus().name());
            cardDetails.put("expiryDate", card.getExpiryDate().toString());
            cardDetails.put("cvv", "***"); // Never expose real CVV
            cardDetails.put("dailyLimit", card.getDailyLimit());
            cardDetails.put("monthlyLimit", card.getMonthlyLimit());
            cardDetails.put("contactlessEnabled", card.isContactlessEnabled());
            cardDetails.put("onlineTransactionEnabled", card.isOnlineTransactionEnabled());
            cardDetails.put("internationalUsageEnabled", card.isInternationalUsageEnabled());
            cardDetails.put("lastUsed", card.getLastUsed() != null ? card.getLastUsed().toString() : null);
            cardDetails.put("createdAt", card.getCreatedAt().toString());
            cardDetails.put("accountNumber", card.getAccount().getAccountNumber());
            cardDetails.put("blockReason", card.getBlockReason());
            
            return cardDetails;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid card ID");
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve card details: " + e.getMessage());
        }
    }

    public Map<String, Object> blockCard(String cardId, Map<String, String> data) {
        try {
            User currentUser = getCurrentUser();
            Long id = Long.parseLong(cardId);
            
            Optional<Card> cardOpt = cardRepository.findById(id);
            if (cardOpt.isEmpty()) {
                throw new RuntimeException("Card not found");
            }
            
            Card card = cardOpt.get();
            
            // Check ownership
            if (!card.getAccount().getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized access to card");
            }
            
            String reason = data.getOrDefault("reason", "User requested block");
            card.blockCard(reason);
            cardRepository.save(card);
            
            // Send real-time update
            webSocketService.sendUserUpdate(currentUser.getId(), "card_blocked", 
                Map.of("cardId", id, "reason", reason));
            
            return Map.of(
                "success", true,
                "message", "Card blocked successfully",
                "cardId", cardId,
                "reason", reason
            );
        } catch (NumberFormatException e) {
            return Map.of(
                "success", false,
                "message", "Invalid card ID"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to block card: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> unblockCard(String cardId) {
        try {
            User currentUser = getCurrentUser();
            Long id = Long.parseLong(cardId);
            
            Optional<Card> cardOpt = cardRepository.findById(id);
            if (cardOpt.isEmpty()) {
                throw new RuntimeException("Card not found");
            }
            
            Card card = cardOpt.get();
            
            // Check ownership
            if (!card.getAccount().getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized access to card");
            }
            
            card.activateCard();
            cardRepository.save(card);
            
            // Send real-time update
            webSocketService.sendUserUpdate(currentUser.getId(), "card_unblocked", 
                Map.of("cardId", id));
            
            return Map.of(
                "success", true,
                "message", "Card unblocked successfully",
                "cardId", cardId
            );
        } catch (NumberFormatException e) {
            return Map.of(
                "success", false,
                "message", "Invalid card ID"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to unblock card: " + e.getMessage()
            );
        }
    }
    
    public Map<String, Object> changeCardPin(String cardId, Map<String, String> data) {
        try {
            User currentUser = getCurrentUser();
            Long id = Long.parseLong(cardId);
            
            Optional<Card> cardOpt = cardRepository.findById(id);
            if (cardOpt.isEmpty()) {
                throw new RuntimeException("Card not found");
            }
            
            Card card = cardOpt.get();
            
            // Check ownership
            if (!card.getAccount().getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized access to card");
            }
            
            String oldPin = data.get("oldPin");
            String newPin = data.get("newPin");
            
            if (oldPin == null || newPin == null) {
                throw new RuntimeException("Both old PIN and new PIN are required");
            }
            
            // Verify old PIN (in production, this should be encrypted comparison)
            if (!oldPin.equals(card.getPin())) {
                throw new RuntimeException("Invalid old PIN");
            }
            
            // Validate new PIN (4 digits)
            if (!newPin.matches("\\d{4}")) {
                throw new RuntimeException("PIN must be exactly 4 digits");
            }
            
            // Update PIN (in production, this should be encrypted)
            card.setPin(newPin);
            cardRepository.save(card);
            
            // Send real-time update
            webSocketService.sendUserUpdate(currentUser.getId(), "card_pin_changed", 
                Map.of("cardId", id));
            
            return Map.of(
                "success", true,
                "message", "Card PIN changed successfully",
                "cardId", cardId
            );
        } catch (NumberFormatException e) {
            return Map.of(
                "success", false,
                "message", "Invalid card ID"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to change PIN: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> setCardLimits(String cardId, Map<String, Object> data) {
        try {
            User currentUser = getCurrentUser();
            Long id = Long.parseLong(cardId);
            
            Optional<Card> cardOpt = cardRepository.findById(id);
            if (cardOpt.isEmpty()) {
                throw new RuntimeException("Card not found");
            }
            
            Card card = cardOpt.get();
            
            // Check ownership
            if (!card.getAccount().getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized access to card");
            }
            
            // Update limits if provided
            if (data.containsKey("dailyLimit")) {
                BigDecimal dailyLimit = new BigDecimal(data.get("dailyLimit").toString());
                if (dailyLimit.compareTo(BigDecimal.ZERO) < 0) {
                    throw new RuntimeException("Daily limit cannot be negative");
                }
                card.setDailyLimit(dailyLimit);
            }
            
            if (data.containsKey("monthlyLimit")) {
                BigDecimal monthlyLimit = new BigDecimal(data.get("monthlyLimit").toString());
                if (monthlyLimit.compareTo(BigDecimal.ZERO) < 0) {
                    throw new RuntimeException("Monthly limit cannot be negative");
                }
                card.setMonthlyLimit(monthlyLimit);
            }
            
            // Update transaction settings
            if (data.containsKey("contactlessEnabled")) {
                card.setContactlessEnabled((Boolean) data.get("contactlessEnabled"));
            }
            
            if (data.containsKey("onlineTransactionEnabled")) {
                card.setOnlineTransactionEnabled((Boolean) data.get("onlineTransactionEnabled"));
            }
            
            if (data.containsKey("internationalUsageEnabled")) {
                card.setInternationalUsageEnabled((Boolean) data.get("internationalUsageEnabled"));
            }
            
            cardRepository.save(card);
            
            // Send real-time update
            webSocketService.sendUserUpdate(currentUser.getId(), "card_limits_updated", 
                Map.of("cardId", id, "dailyLimit", card.getDailyLimit(), "monthlyLimit", card.getMonthlyLimit()));
            
            return Map.of(
                "success", true,
                "message", "Card limits updated successfully",
                "cardId", cardId,
                "dailyLimit", card.getDailyLimit(),
                "monthlyLimit", card.getMonthlyLimit()
            );
        } catch (NumberFormatException e) {
            return Map.of(
                "success", false,
                "message", "Invalid card ID or limit values"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to update limits: " + e.getMessage()
            );
        }
    }

    public List<Map<String, Object>> getCardTransactions(String cardId, String fromDate, String toDate) {
        try {
            User currentUser = getCurrentUser();
            Long id = Long.parseLong(cardId);
            
            Optional<Card> cardOpt = cardRepository.findById(id);
            if (cardOpt.isEmpty()) {
                throw new RuntimeException("Card not found");
            }
            
            Card card = cardOpt.get();
            
            // Check ownership
            if (!card.getAccount().getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized access to card");
            }
            
            // Get transactions for the card's account
            // Filter by card transactions (this would need transaction type filtering in real implementation)
            List<Transaction> transactions = transactionRepository.findByAccountId(card.getAccount().getId(), 
                PageRequest.of(0, 50)).getContent();
            
            return transactions.stream()
                    .filter(t -> t.getNote() != null && t.getNote().toLowerCase().contains("card"))
                    .map(transaction -> {
                        Map<String, Object> txnMap = new HashMap<>();
                        txnMap.put("id", transaction.getId());
                        txnMap.put("transactionId", transaction.getTransactionId());
                        txnMap.put("amount", transaction.getAmount());
                        txnMap.put("type", transaction.getType().name());
                        txnMap.put("description", transaction.getNote());
                        txnMap.put("date", transaction.getCreatedAt().toString());
                        txnMap.put("status", transaction.getStatus().name());
                        return txnMap;
                    }).collect(java.util.stream.Collectors.toList());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid card ID");
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve card transactions: " + e.getMessage());
        }
    }

    public Map<String, Object> applyForCard(Map<String, Object> data) {
        try {
            User currentUser = getCurrentUser();
            
            String cardTypeStr = (String) data.get("cardType");
            String accountNumber = (String) data.get("accountNumber");
            
            if (cardTypeStr == null || accountNumber == null) {
                throw new RuntimeException("Card type and account number are required");
            }
            
            // Parse card type
            CardType cardType;
            try {
                cardType = CardType.valueOf(cardTypeStr.toUpperCase());
            } catch (Exception e) {
                throw new RuntimeException("Invalid card type");
            }
            
            // Validate account ownership
            Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
            if (accountOpt.isEmpty()) {
                throw new RuntimeException("Account not found");
            }
            
            Account account = accountOpt.get();
            if (!account.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized access to account");
            }
            
            // Check if user already has this type of card for this account
            long existingCards = cardRepository.countByUserIdAndCardType(currentUser.getId(), cardType);
            if (existingCards >= 2) { // Limit 2 cards per type
                throw new RuntimeException("You already have maximum allowed " + cardType.name().toLowerCase() + " cards");
            }
            
            // Create new card
            Card newCard = new Card(account, currentUser.getUsername());
            newCard.setCardType(cardType);
            newCard.setStatus(CardStatus.ACTIVE); // Auto-approve for now
            
            Card savedCard = cardRepository.save(newCard);
            
            // Send real-time update
            webSocketService.sendUserUpdate(currentUser.getId(), "card_application_approved", 
                Map.of("cardId", savedCard.getId(), "cardType", cardType.name()));
            
            return Map.of(
                "success", true,
                "message", "Card application approved and card created",
                "cardId", savedCard.getId(),
                "cardNumber", savedCard.getMaskedCardNumber(),
                "cardType", cardType.name(),
                "status", savedCard.getStatus().name()
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Card application failed: " + e.getMessage()
            );
        }
    }
    
    public List<Map<String, Object>> loans() {
        List<LoanApplication> loans = getAllLoans();
        return loans.stream().map(loan -> Map.<String, Object>of(
            "id", loan.getId(),
            "amount", loan.getAmount(),
            "status", loan.getStatus().toString()
        )).toList();
    }
    
    public Map<String, Object> getLoanDetails(String loanId) {
        return Map.of("loanId", loanId, "amount", 500000.00, "status", "APPROVED", "interestRate", 8.5);
    }
    
    public Map<String, Object> repayLoan(String loanId, Map<String, Object> data) {
        return Map.of("message", "Loan payment completed", "loanId", loanId, "transactionId", "LOAN" + System.currentTimeMillis());
    }
    
    public Map<String, Object> getLoanRepaymentSchedule(String loanId) {
        return Map.of("loanId", loanId, "schedule", Arrays.asList(
            Map.of("emiNumber", 1, "amount", 45000.00, "dueDate", "2024-02-01", "status", "PENDING"),
            Map.of("emiNumber", 2, "amount", 45000.00, "dueDate", "2024-03-01", "status", "PENDING")
        ));
    }
    
    public List<Map<String, Object>> getFixedDeposits() {
        return Arrays.asList(
            Map.of("id", "FD001", "amount", 100000.00, "interestRate", 7.5, "maturityDate", "2025-01-15"),
            Map.of("id", "FD002", "amount", 250000.00, "interestRate", 8.0, "maturityDate", "2025-06-15")
        );
    }
    
    public Map<String, Object> createFixedDeposit(Map<String, Object> data) {
        return Map.of("message", "Fixed deposit created", "fdId", "FD" + System.currentTimeMillis());
    }
    
    public List<Map<String, Object>> getRecurringDeposits() {
        return Arrays.asList(
            Map.of("id", "RD001", "monthlyAmount", 5000.00, "tenure", "36 months", "maturityAmount", 190000.00)
        );
    }
    
    public Map<String, Object> createRecurringDeposit(Map<String, Object> data) {
        return Map.of("message", "Recurring deposit created", "rdId", "RD" + System.currentTimeMillis());
    }
    
    public List<Map<String, Object>> getMutualFunds() {
        return Arrays.asList(
            Map.of("id", "MF001", "name", "Equity Growth Fund", "nav", 45.67, "units", 1000, "currentValue", 45670.00),
            Map.of("id", "MF002", "name", "Debt Fund", "nav", 25.45, "units", 500, "currentValue", 12725.00)
        );
    }
    
    public Map<String, Object> investInMutualFund(Map<String, Object> data) {
        return Map.of("message", "Mutual fund investment completed", "transactionId", "MF" + System.currentTimeMillis());
    }
    
    public Map<String, Object> getProfile() {
        return Map.of(
            "name", "Demo User",
            "email", "demo@example.com",
            "phone", "+91-9876543210",
            "address", "123 Demo Street, Demo City"
        );
    }
    
    public Map<String, Object> updateProfile(Map<String, Object> data) {
        try {
            // Get current user from security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
            
            if (currentUser == null) {
                return Map.of("success", false, "message", "User not found");
            }

            // Update basic user fields if provided
            if (data.containsKey("email")) {
                String newEmail = (String) data.get("email");
                if (newEmail != null && !newEmail.trim().isEmpty()) {
                    // Check if email already exists for another user
                    Optional<User> existingUser = userRepository.findByEmail(newEmail);
                    if (existingUser.isPresent() && !existingUser.get().getId().equals(currentUser.getId())) {
                        return Map.of("success", false, "message", "Email already exists");
                    }
                    currentUser.setEmail(newEmail);
                }
            }

            if (data.containsKey("username")) {
                String newUsername = (String) data.get("username");
                if (newUsername != null && !newUsername.trim().isEmpty()) {
                    // Check if username already exists for another user
                    Optional<User> existingUser = userRepository.findByUsername(newUsername);
                    if (existingUser.isPresent() && !existingUser.get().getId().equals(currentUser.getId())) {
                        return Map.of("success", false, "message", "Username already exists");
                    }
                    currentUser.setUsername(newUsername);
                }
            }

            // Save updated user
            userRepository.save(currentUser);

            // Handle UserProfile data
            String phoneNumber = (String) data.get("phone");
            String firstName = (String) data.get("firstName");
            String lastName = (String) data.get("lastName");
            String address = (String) data.get("address");

            // Find or create user profile
            Optional<UserProfile> existingProfile = userProfileRepository.findByUser(currentUser);
            UserProfile profile;
            
            if (existingProfile.isPresent()) {
                profile = existingProfile.get();
                profile.setUpdatedAt(LocalDateTime.now());
            } else {
                profile = new UserProfile();
                profile.setUser(currentUser);
                profile.setCreatedAt(LocalDateTime.now());
                profile.setUpdatedAt(LocalDateTime.now());
            }

            // Update profile fields
            if (phoneNumber != null) {
                profile.setPhoneNumber(phoneNumber);
            }
            if (firstName != null) {
                profile.setFirstName(firstName);
            }
            if (lastName != null) {
                profile.setLastName(lastName);
            }
            if (address != null) {
                profile.setAddress(address);
            }

            userProfileRepository.save(profile);

            return Map.of(
                "success", true, 
                "message", "Profile updated successfully",
                "user", Map.of(
                    "id", currentUser.getId(),
                    "username", currentUser.getUsername(),
                    "email", currentUser.getEmail(),
                    "role", currentUser.getRole().name()
                )
            );
            
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error updating profile: " + e.getMessage());
        }
    }
    
    public Map<String, Object> changePassword(String oldPassword, String newPassword) {
        return Map.of("message", "Password changed successfully");
    }
    
    public Map<String, Object> enableTwoFactorAuth(Map<String, String> data) {
        return Map.of("message", "Two-factor authentication enabled");
    }
    
    public Map<String, Object> disableTwoFactorAuth(Map<String, String> data) {
        return Map.of("message", "Two-factor authentication disabled");
    }
    
    public Map<String, Object> getSecuritySettings() {
        return Map.of(
            "twoFactorEnabled", false,
            "loginNotifications", true,
            "transactionAlerts", true
        );
    }
    
    public Map<String, Object> updateSecuritySettings(Map<String, Object> data) {
        return Map.of("message", "Security settings updated");
    }
    
    public List<Map<String, Object>> loginHistory() {
        return Arrays.asList(
            Map.of("timestamp", "2024-01-15 10:30:00", "ipAddress", "192.168.1.100", "device", "Chrome/Windows"),
            Map.of("timestamp", "2024-01-14 15:45:00", "ipAddress", "192.168.1.100", "device", "Firefox/Windows")
        );
    }
    
    public List<Map<String, Object>> getTrustedDevices() {
        return Arrays.asList(
            Map.of("id", "DEV001", "name", "Personal Laptop", "lastUsed", "2024-01-15", "status", "ACTIVE"),
            Map.of("id", "DEV002", "name", "Mobile Phone", "lastUsed", "2024-01-14", "status", "ACTIVE")
        );
    }
    
    public Map<String, Object> revokeTrustedDevice(String deviceId) {
        return Map.of("message", "Trusted device revoked", "deviceId", deviceId);
    }
    
    public List<Map<String, Object>> getSupportTickets() {
        return Arrays.asList(
            Map.of("id", "TKT001", "subject", "Transaction issue", "status", "OPEN", "createdDate", "2024-01-10"),
            Map.of("id", "TKT002", "subject", "Card replacement", "status", "RESOLVED", "createdDate", "2024-01-08")
        );
    }
    
    public Map<String, Object> createSupportTicket(Map<String, Object> data) {
        return Map.of("message", "Support ticket created", "ticketId", "TKT" + System.currentTimeMillis());
    }
    
    public Map<String, Object> getSupportTicketDetails(String ticketId) {
        return Map.of("ticketId", ticketId, "subject", "Demo Ticket", "status", "OPEN", "description", "Demo ticket description");
    }
    
    public Map<String, Object> replySupportTicket(String ticketId, Map<String, String> data) {
        return Map.of("message", "Reply added to ticket", "ticketId", ticketId);
    }
    
    public Map<String, Object> closeSupportTicket(String ticketId) {
        return Map.of("message", "Support ticket closed", "ticketId", ticketId);
    }
    
    public Map<String, Object> reportFraud(Map<String, Object> data) {
        return Map.of("message", "Fraud report submitted", "reportId", "FR" + System.currentTimeMillis());
    }
    
    public List<Map<String, Object>> getServiceRequests() {
        return Arrays.asList(
            Map.of("id", "SR001", "type", "Checkbook request", "status", "PROCESSING"),
            Map.of("id", "SR002", "type", "Statement request", "status", "COMPLETED")
        );
    }
    
    public Map<String, Object> createServiceRequest(Map<String, Object> data) {
        return Map.of("message", "Service request created", "requestId", "SR" + System.currentTimeMillis());
    }
    
    public List<Map<String, Object>> getFAQ() {
        return Arrays.asList(
            Map.of("question", "How to reset password?", "answer", "Click on forgot password link"),
            Map.of("question", "How to block card?", "answer", "Call customer care or use mobile app")
        );
    }
    
    public Map<String, Object> submitFeedback(Map<String, Object> data) {
        return Map.of("message", "Feedback submitted successfully");
    }
    
    public List<Map<String, Object>> getNotifications() {
        return Arrays.asList(
            Map.of("id", "NOT001", "message", "Your transaction of Rs. 5000 is successful", "read", false, "timestamp", "2024-01-15 10:30:00"),
            Map.of("id", "NOT002", "message", "Your card ending with 1234 has been blocked", "read", true, "timestamp", "2024-01-14 15:45:00")
        );
    }
    
    public Map<String, Object> markNotificationRead(String notificationId) {
        return Map.of("message", "Notification marked as read", "notificationId", notificationId);
    }
    
    public Map<String, Object> updateNotificationPreferences(Map<String, Object> data) {
        return Map.of("message", "Notification preferences updated");
    }
    
    public List<Map<String, Object>> getAccountAlerts() {
        return Arrays.asList(
            Map.of("id", "ALT001", "type", "BALANCE_LOW", "threshold", 1000.00, "enabled", true),
            Map.of("id", "ALT002", "type", "LARGE_TRANSACTION", "threshold", 50000.00, "enabled", true)
        );
    }
    
    public Map<String, Object> setAccountAlerts(Map<String, Object> data) {
        return Map.of("message", "Account alerts updated");
    }

    // Admin service methods for user management
    public long getTotalUserCount() {
        return userRepository.count();
    }

    public long getActiveUserCount() {
        return userRepository.countByActiveTrue();
    }

    public long getLockedUserCount() {
        return userRepository.countByLockedTrue();
    }

    public long getNewUsersToday() {
        // TODO: Implement proper date filtering
        return userRepository.count(); // Placeholder
    }

    public org.springframework.data.domain.Page<User> getAllUsers(org.springframework.data.domain.Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public org.springframework.data.domain.Page<User> searchUsers(String search, org.springframework.data.domain.Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User createUserByAdmin(Map<String, Object> userRequest) {
        // TODO: Implement user creation by admin
        User user = new User();
        user.setUsername((String) userRequest.get("username"));
        user.setEmail((String) userRequest.get("email"));
        // Set other fields
        return userRepository.save(user);
    }

    public User updateUserByAdmin(Long userId, Map<String, Object> userUpdate) {
        User user = getUserById(userId);
        // TODO: Update user fields based on userUpdate map
        return userRepository.save(user);
    }

    public void lockUser(Long userId) {
        User user = getUserById(userId);
        user.setLocked(true);
        userRepository.save(user);
    }

    public void unlockUser(Long userId) {
        User user = getUserById(userId);
        user.setLocked(false);
        user.setFailedAttempts(0);
        userRepository.save(user);
    }

    public String resetUserPassword(Long userId) {
        User user = getUserById(userId);
        String tempPassword = "TempPass" + System.currentTimeMillis();
        user.setPassword(tempPassword); // Set the temporary password
        userRepository.save(user); // Save the user with new password
        // TODO: Encode password properly
        return tempPassword;
    }

    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(false); // Soft delete
        userRepository.save(user);
    }

    public long getFailedLoginAttempts() {
        return userRepository.countByFailedAttemptsGreaterThan(0);
    }

    // Methods needed by SuperAdminController
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User createUser(String username, String email, String password, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role != null ? role : Role.USER);
        user.setActive(true);
        user.setLocked(false);
        user.setFailedAttempts(0);
        
        return userRepository.save(user);
    }
}
