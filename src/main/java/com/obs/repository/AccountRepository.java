package com.obs.repository;

import com.obs.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserUsername(String username);
    List<Account> findByStatus(AccountStatus status);
    List<Account> findByUser_Id(Long userId);
    Optional<Account> findByAccountNumber(String accountNumber);
}
