package com.obs.repository;

import com.obs.model.Card;
import com.obs.model.CardStatus;
import com.obs.model.CardType;
import com.obs.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    
    // Find cards by account
    List<Card> findByAccount(Account account);
    List<Card> findByAccountId(Long accountId);
    
    // Find cards by user (through account relationship)
    @Query("SELECT c FROM Card c WHERE c.account.user.id = :userId")
    List<Card> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Card c WHERE c.account.user.id = :userId AND c.status = :status")
    List<Card> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") CardStatus status);
    
    // Find by card number
    Optional<Card> findByCardNumber(String cardNumber);
    boolean existsByCardNumber(String cardNumber);
    
    // Find by card type
    @Query("SELECT c FROM Card c WHERE c.account.user.id = :userId AND c.cardType = :cardType")
    List<Card> findByUserIdAndCardType(@Param("userId") Long userId, @Param("cardType") CardType cardType);
    
    // Find active cards
    @Query("SELECT c FROM Card c WHERE c.account.user.id = :userId AND c.status = 'ACTIVE'")
    List<Card> findActiveCardsByUserId(@Param("userId") Long userId);
    
    // Find blocked cards
    @Query("SELECT c FROM Card c WHERE c.account.user.id = :userId AND c.status = 'BLOCKED'")
    List<Card> findBlockedCardsByUserId(@Param("userId") Long userId);
    
    // Count cards by user and type
    @Query("SELECT COUNT(c) FROM Card c WHERE c.account.user.id = :userId AND c.cardType = :cardType")
    long countByUserIdAndCardType(@Param("userId") Long userId, @Param("cardType") CardType cardType);
    
    // Find cards expiring soon
    @Query("SELECT c FROM Card c WHERE c.account.user.id = :userId AND c.expiryDate <= :targetDate AND c.status = 'ACTIVE'")
    List<Card> findCardsExpiringSoon(@Param("userId") Long userId, @Param("targetDate") java.time.LocalDate targetDate);
    
    // Delete cards by user ID (for cascade deletion)
    @Query("DELETE FROM Card c WHERE c.account.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
