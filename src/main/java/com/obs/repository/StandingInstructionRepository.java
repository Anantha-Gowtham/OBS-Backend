package com.obs.repository;

import com.obs.model.StandingInstruction;
import com.obs.model.InstructionStatus;
import com.obs.model.InstructionFrequency;
import com.obs.model.InstructionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StandingInstructionRepository extends JpaRepository<StandingInstruction, Long> {

    // Find by user
    List<StandingInstruction> findByUserId(Long userId);
    
    Page<StandingInstruction> findByUserId(Long userId, Pageable pageable);

    // Find by user and status
    List<StandingInstruction> findByUserIdAndStatus(Long userId, InstructionStatus status);
    
    Page<StandingInstruction> findByUserIdAndStatus(Long userId, InstructionStatus status, Pageable pageable);

    // Find by instruction ID and user (for security)
    Optional<StandingInstruction> findByInstructionIdAndUserId(String instructionId, Long userId);

    // Find by account
    List<StandingInstruction> findByFromAccountAndUserId(String fromAccount, Long userId);
    
    List<StandingInstruction> findByToAccountAndUserId(String toAccount, Long userId);

    // Find by frequency
    List<StandingInstruction> findByUserIdAndFrequency(Long userId, InstructionFrequency frequency);

    // Find by type
    List<StandingInstruction> findByUserIdAndInstructionType(Long userId, InstructionType type);

    // Find due instructions (for execution)
    @Query("SELECT si FROM StandingInstruction si WHERE si.status = 'ACTIVE' " +
           "AND si.nextExecutionDate <= :currentDate")
    List<StandingInstruction> findDueInstructions(@Param("currentDate") LocalDate currentDate);

    // Find due instructions for specific user
    @Query("SELECT si FROM StandingInstruction si WHERE si.userId = :userId " +
           "AND si.status = 'ACTIVE' AND si.nextExecutionDate <= :currentDate")
    List<StandingInstruction> findDueInstructionsByUser(@Param("userId") Long userId, 
                                                       @Param("currentDate") LocalDate currentDate);

    // Find expiring instructions (where endDate is approaching)
    @Query("SELECT si FROM StandingInstruction si WHERE si.userId = :userId " +
           "AND si.status = 'ACTIVE' AND si.endDate IS NOT NULL " +
           "AND si.endDate BETWEEN :fromDate AND :toDate")
    List<StandingInstruction> findExpiringInstructions(@Param("userId") Long userId,
                                                       @Param("fromDate") LocalDate fromDate,
                                                       @Param("toDate") LocalDate toDate);

    // Find instructions by amount range
    @Query("SELECT si FROM StandingInstruction si WHERE si.userId = :userId " +
           "AND si.amount BETWEEN :minAmount AND :maxAmount")
    List<StandingInstruction> findByAmountRange(@Param("userId") Long userId,
                                               @Param("minAmount") java.math.BigDecimal minAmount,
                                               @Param("maxAmount") java.math.BigDecimal maxAmount);

    // Find recent instructions
    @Query("SELECT si FROM StandingInstruction si WHERE si.userId = :userId " +
           "AND si.createdAt >= :fromDate ORDER BY si.createdAt DESC")
    List<StandingInstruction> findRecentInstructions(@Param("userId") Long userId,
                                                     @Param("fromDate") java.time.LocalDateTime fromDate);

    // Count by status
    @Query("SELECT COUNT(si) FROM StandingInstruction si WHERE si.userId = :userId AND si.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") InstructionStatus status);

    // Find failed instructions
    @Query("SELECT si FROM StandingInstruction si WHERE si.userId = :userId " +
           "AND si.status = 'FAILED' ORDER BY si.updatedAt DESC")
    List<StandingInstruction> findFailedInstructions(@Param("userId") Long userId);

    // Delete by user (cascade when user is deleted)
    void deleteByUserId(Long userId);

    // Find instructions with high execution count (frequently used)
    @Query("SELECT si FROM StandingInstruction si WHERE si.userId = :userId " +
           "AND si.executionCount >= :minCount ORDER BY si.executionCount DESC")
    List<StandingInstruction> findHighUsageInstructions(@Param("userId") Long userId,
                                                        @Param("minCount") int minCount);
}