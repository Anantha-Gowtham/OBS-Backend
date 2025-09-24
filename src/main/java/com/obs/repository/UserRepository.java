package com.obs.repository;

import com.obs.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // Admin service methods
    long countByActiveTrue();
    long countByLockedTrue();
    long countByFailedAttemptsGreaterThan(int attempts);
    
    // Search methods
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String username, String email, Pageable pageable);
}
