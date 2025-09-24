package com.obs.repository;

import com.obs.model.BillPayment;
import com.obs.model.BillType;
import com.obs.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {
    
    // Find bill payments by user ID
    List<BillPayment> findByUserId(Long userId);
    
    // Find bill payments by user ID and status
    List<BillPayment> findByUserIdAndStatus(Long userId, PaymentStatus status);
    
    // Find bill payment by payment ID
    Optional<BillPayment> findByPaymentId(String paymentId);
    
    // Find bill payments by bill type
    List<BillPayment> findByUserIdAndBillType(Long userId, BillType billType);
    
    // Find bill payments by consumer number
    List<BillPayment> findByUserIdAndConsumerNumber(Long userId, String consumerNumber);
    
    // Find bill payments by biller name
    List<BillPayment> findByUserIdAndBillerNameContainingIgnoreCase(Long userId, String billerName);
    
    // Find recent bill payments
    @Query("SELECT bp FROM BillPayment bp WHERE bp.userId = :userId " +
           "ORDER BY bp.paymentDate DESC")
    List<BillPayment> findRecentBillPayments(@Param("userId") Long userId);
    
    // Find bill payments within date range
    @Query("SELECT bp FROM BillPayment bp WHERE bp.userId = :userId " +
           "AND bp.paymentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY bp.paymentDate DESC")
    List<BillPayment> findBillPaymentsBetweenDates(@Param("userId") Long userId, 
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    // Find bill payments by amount range
    @Query("SELECT bp FROM BillPayment bp WHERE bp.userId = :userId " +
           "AND bp.amount BETWEEN :minAmount AND :maxAmount " +
           "ORDER BY bp.paymentDate DESC")
    List<BillPayment> findBillPaymentsByAmountRange(@Param("userId") Long userId,
                                                    @Param("minAmount") BigDecimal minAmount,
                                                    @Param("maxAmount") BigDecimal maxAmount);
    
    // Count bill payments by status
    long countByUserIdAndStatus(Long userId, PaymentStatus status);
    
    // Sum of bill payments by user
    @Query("SELECT SUM(bp.amount) FROM BillPayment bp WHERE bp.userId = :userId " +
           "AND bp.status = :status")
    BigDecimal sumAmountByUserIdAndStatus(@Param("userId") Long userId, 
                                         @Param("status") PaymentStatus status);
    
    // Find pending bill payments with due dates
    @Query("SELECT bp FROM BillPayment bp WHERE bp.userId = :userId " +
           "AND bp.status = 'PENDING' AND bp.dueDate IS NOT NULL " +
           "AND bp.dueDate <= :date ORDER BY bp.dueDate ASC")
    List<BillPayment> findPendingBillsWithDueDateBefore(@Param("userId") Long userId,
                                                        @Param("date") LocalDateTime date);
    
    // Find bill payments by transaction ID
    Optional<BillPayment> findByTransactionId(String transactionId);
    
    // Find failed bill payments
    List<BillPayment> findByUserIdAndStatusOrderByPaymentDateDesc(Long userId, PaymentStatus status);
    
    // Delete bill payments by user ID (for cascade deletion)
    void deleteByUserId(Long userId);
    
    // Find bill payments by multiple bill types
    @Query("SELECT bp FROM BillPayment bp WHERE bp.userId = :userId " +
           "AND bp.billType IN :billTypes ORDER BY bp.paymentDate DESC")
    List<BillPayment> findByUserIdAndBillTypeIn(@Param("userId") Long userId, 
                                               @Param("billTypes") List<BillType> billTypes);
    
    // Count total bill payments for user
    long countByUserId(Long userId);
    
    // Find monthly bill payment statistics
    @Query("SELECT MONTH(bp.paymentDate) as month, COUNT(bp) as count, SUM(bp.amount) as total " +
           "FROM BillPayment bp WHERE bp.userId = :userId " +
           "AND YEAR(bp.paymentDate) = :year AND bp.status = 'COMPLETED' " +
           "GROUP BY MONTH(bp.paymentDate)")
    List<Object[]> findMonthlyBillPaymentStats(@Param("userId") Long userId, 
                                              @Param("year") int year);
}