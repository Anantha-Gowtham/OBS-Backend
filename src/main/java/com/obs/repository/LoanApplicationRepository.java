package com.obs.repository;

import com.obs.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByStatus(LoanStatus status);
    List<LoanApplication> findByUserUsername(String username);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM LoanApplication l WHERE l.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
