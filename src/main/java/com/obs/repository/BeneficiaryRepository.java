package com.obs.repository;

import com.obs.model.Beneficiary;
import com.obs.model.BeneficiaryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    
    // Find beneficiaries by user ID
    List<Beneficiary> findByUserIdAndIsActiveTrue(Long userId);
    
    // Find all beneficiaries by user ID (including inactive)
    List<Beneficiary> findByUserId(Long userId);
    
    // Find beneficiary by account number and user ID
    Optional<Beneficiary> findByAccountNumberAndUserId(String accountNumber, Long userId);
    
    // Find beneficiary by account number (global search)
    Optional<Beneficiary> findByAccountNumber(String accountNumber);
    
    // Find beneficiaries by type
    List<Beneficiary> findByUserIdAndBeneficiaryTypeAndIsActiveTrue(Long userId, BeneficiaryType type);
    
    // Find verified beneficiaries
    List<Beneficiary> findByUserIdAndIsVerifiedTrueAndIsActiveTrue(Long userId);
    
    // Check if beneficiary exists by account number for user
    boolean existsByAccountNumberAndUserId(String accountNumber, Long userId);
    
    // Find beneficiaries by nickname
    List<Beneficiary> findByUserIdAndNicknameContainingIgnoreCaseAndIsActiveTrue(Long userId, String nickname);
    
    // Find beneficiaries by beneficiary name
    List<Beneficiary> findByUserIdAndBeneficiaryNameContainingIgnoreCaseAndIsActiveTrue(Long userId, String name);
    
    // Count active beneficiaries for user
    long countByUserIdAndIsActiveTrue(Long userId);
    
    // Find recently used beneficiaries
    @Query("SELECT b FROM Beneficiary b WHERE b.userId = :userId AND b.isActive = true " +
           "AND b.lastUsed IS NOT NULL ORDER BY b.lastUsed DESC")
    List<Beneficiary> findRecentlyUsedBeneficiaries(@Param("userId") Long userId);
    
    // Find beneficiaries by bank name
    List<Beneficiary> findByUserIdAndBankNameContainingIgnoreCaseAndIsActiveTrue(Long userId, String bankName);
    
    // Find beneficiaries by IFSC code
    List<Beneficiary> findByUserIdAndIfscCodeAndIsActiveTrue(Long userId, String ifscCode);
    
    // Delete beneficiaries by user ID (for cascade deletion)
    void deleteByUserId(Long userId);
    
    // Find unverified beneficiaries
    List<Beneficiary> findByUserIdAndIsVerifiedFalseAndIsActiveTrue(Long userId);
}