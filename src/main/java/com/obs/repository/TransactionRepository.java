package com.obs.repository;

import com.obs.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);
    java.util.List<Transaction> findByStatus(TransactionStatus status);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Transaction t WHERE t.account.id = :accountId")
    void deleteByAccountId(@Param("accountId") Long accountId);
}
