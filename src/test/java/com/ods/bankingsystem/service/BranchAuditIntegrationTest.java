package com.ods.bankingsystem.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.ods.bankingsystem.model.Branch;
import com.ods.bankingsystem.model.Role;
import com.ods.bankingsystem.model.User;
import com.ods.bankingsystem.repository.BranchAuditRepository;
import com.ods.bankingsystem.repository.UserRepository;
import com.ods.bankingsystem.service.BranchService;

@SpringBootTest
@Transactional
class BranchAuditIntegrationTest {

    @Autowired
    private BranchService branchService;


    @Autowired
    private BranchAuditRepository branchAuditRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createUpdateDeleteBranchGeneratesAudits() {
        // create an admin user to set context manually (simplified)
    userRepository.findByUsername("auditadmin").orElseGet(() -> {
            User u = new User();
            u.setUsername("auditadmin");
            u.setEmail("auditadmin@example.com");
            u.setPassword(passwordEncoder.encode("Password1!"));
            u.setFirstName("Audit");
            u.setLastName("Admin");
            u.setRole(Role.ADMIN);
            return userRepository.save(u);
        });

        Branch b = new Branch();
        b.setBranchCode("AUDT01");
        b.setBranchName("Audit Test");
        b.setAddress("Addr");
        Branch created = branchService.create(b);

        created.setBranchName("Audit Test Updated");
        branchService.update(created.getId(), created);

        branchService.delete(created.getId());

        var audits = branchAuditRepository.findByBranchIdOrderByTimestampDesc(created.getId());
        Assertions.assertTrue(audits.size() >= 3, "Should have at least 3 audit records");
    }
}
