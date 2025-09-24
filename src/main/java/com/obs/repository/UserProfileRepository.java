package com.obs.repository;

import com.obs.model.UserProfile;
import com.obs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser(User user);
    Optional<UserProfile> findByPhoneNumber(String phoneNumber);
    Optional<UserProfile> findByAadhaarNumber(String aadhaarNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByAadhaarNumber(String aadhaarNumber);
}