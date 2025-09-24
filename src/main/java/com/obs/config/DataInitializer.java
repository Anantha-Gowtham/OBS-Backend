package com.obs.config;

import com.obs.model.*;
import com.obs.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;

@Component
public class DataInitializer implements CommandLineRunner {

    // Utility method to convert LocalDateTime to Instant
    private Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanApplicationRepository loanRepository;
    private final KycRequestRepository kycRepository;
    private final BranchRepository branchRepository;

    public DataInitializer(UserRepository userRepository, 
                          PasswordEncoder passwordEncoder,
                          AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          LoanApplicationRepository loanRepository,
                          KycRequestRepository kycRepository,
                          BranchRepository branchRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.loanRepository = loanRepository;
        this.kycRepository = kycRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== DataInitializer Running ===");
        
        // Create default super_admin user if not exists
        if (!userRepository.existsByUsername("super_admin")) {
            System.out.println("Creating super_admin user...");
            User superAdmin = new User();
            superAdmin.setUsername("super_admin");
            superAdmin.setEmail("superadmin@obs.com");
            superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
            superAdmin.setRole(Role.SUPER_ADMIN);
            superAdmin.setActive(true);
            superAdmin.setLocked(false);
            superAdmin.setFailedAttempts(0);
            userRepository.save(superAdmin);
            System.out.println("super_admin user created successfully!");
        } else {
            System.out.println("super_admin user already exists");
        }

        // Create default admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@obs.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setLocked(false);
            admin.setFailedAttempts(0);
            userRepository.save(admin);
        }

        // Create default manager user if not exists
        if (!userRepository.existsByUsername("manager")) {
            User manager = new User();
            manager.setUsername("manager");
            manager.setEmail("manager@obs.com");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setRole(Role.MANAGER);
            manager.setActive(true);
            manager.setLocked(false);
            manager.setFailedAttempts(0);
            userRepository.save(manager);
        }

        // Create default employee user if not exists
        if (!userRepository.existsByUsername("employee")) {
            User employee = new User();
            employee.setUsername("employee");
            employee.setEmail("employee@obs.com");
            employee.setPassword(passwordEncoder.encode("employee123"));
            employee.setRole(Role.EMPLOYEE);
            employee.setActive(true);
            employee.setLocked(false);
            employee.setFailedAttempts(0);
            userRepository.save(employee);
        }

        // Create default user if not exists
        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@obs.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(Role.USER);
            user.setActive(true);
            user.setLocked(false);
            user.setFailedAttempts(0);
            userRepository.save(user);
        }

        // Create sample branch
        if (branchRepository.count() == 0) {
            Branch branch = new Branch();
            branch.setName("Main Branch");
            branch.setCity("Mumbai");
            branchRepository.save(branch);
        }

        // Create sample accounts
        createSampleAccounts();
        
        // Create sample transactions
        createSampleTransactions();
        
        // Create sample loan applications
        createSampleLoans();
        
        // Create sample KYC requests
        createSampleKycRequests();
    }
    
    private void createSampleAccounts() {
        if (accountRepository.count() == 0) {
            User user = userRepository.findByUsername("user").orElse(null);
            if (user != null) {
                Account savings = new Account();
                savings.setAccountNumber("ACC001234567890");
                savings.setAccountType("SAVINGS");
                savings.setBalance(new BigDecimal("25000.00"));
                savings.setStatus(AccountStatus.ACTIVE);
                savings.setUser(user);
                savings.setCreatedAt(toInstant(LocalDateTime.now().minusDays(30)));
                accountRepository.save(savings);

                Account current = new Account();
                current.setAccountNumber("ACC009876543210");
                current.setAccountType("CURRENT");
                current.setBalance(new BigDecimal("50000.00"));
                current.setStatus(AccountStatus.ACTIVE);
                current.setUser(user);
                current.setCreatedAt(toInstant(LocalDateTime.now().minusDays(15)));
                accountRepository.save(current);
            }
        }
    }
    
    private void createSampleTransactions() {
        if (transactionRepository.count() == 0 && accountRepository.count() > 0) {
            Account account = accountRepository.findAll().get(0);
            
            // Credit transaction
            Transaction credit = new Transaction();
            credit.setAccount(account);
            credit.setType(TransactionType.DEPOSIT);
            credit.setAmount(new BigDecimal("5000.00"));
            credit.setNote("Salary credit");
            credit.setStatus(TransactionStatus.COMPLETED);
            credit.setCreatedAt(toInstant(LocalDateTime.now().minusDays(5)));
            transactionRepository.save(credit);

            // Debit transaction
            Transaction debit = new Transaction();
            debit.setAccount(account);
            debit.setType(TransactionType.WITHDRAWAL);
            debit.setAmount(new BigDecimal("1500.00"));
            debit.setNote("ATM withdrawal");
            debit.setStatus(TransactionStatus.COMPLETED);
            debit.setCreatedAt(toInstant(LocalDateTime.now().minusDays(3)));
            transactionRepository.save(debit);

            // Pending transaction
            Transaction pending = new Transaction();
            pending.setAccount(account);
            pending.setType(TransactionType.TRANSFER);
            pending.setAmount(new BigDecimal("2000.00"));
            pending.setNote("Online transfer");
            pending.setStatus(TransactionStatus.PENDING);
            pending.setCreatedAt(toInstant(LocalDateTime.now().minusHours(2)));
            transactionRepository.save(pending);
        }
    }
    
    private void createSampleLoans() {
        if (loanRepository.count() == 0) {
            User user = userRepository.findByUsername("user").orElse(null);
            if (user != null) {
                LoanApplication loan = new LoanApplication();
                loan.setUser(user);
                loan.setAmount(new BigDecimal("100000.00"));
                loan.setStatus(LoanStatus.PENDING);
                loan.setCreatedAt(toInstant(LocalDateTime.now().minusDays(7)));
                loanRepository.save(loan);
            }
        }
    }
    
    private void createSampleKycRequests() {
        if (kycRepository.count() == 0) {
            User user = userRepository.findByUsername("user").orElse(null);
            if (user != null) {
                KycRequest kyc = new KycRequest();
                kyc.setUser(user);
                kyc.setStatus(KycStatus.PENDING);
                kyc.setComments("Document verification required");
                kyc.setCreatedAt(toInstant(LocalDateTime.now().minusDays(2)));
                kycRepository.save(kyc);
            }
        }
    }
}
