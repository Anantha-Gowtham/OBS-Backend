package com.obs.service;

import com.obs.model.User;
import com.obs.model.UserProfile;
import com.obs.model.Role;
import com.obs.repository.UserRepository;
import com.obs.repository.UserProfileRepository;
import com.obs.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SuperAdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Get all users in the system
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Promote a user to the next higher role
     */
    @Transactional
    public boolean promoteUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Role currentRole = user.getRole();
            Role newRole = getNextHigherRole(currentRole);
            
            if (newRole != null && newRole != currentRole) {
                user.setRole(newRole);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    /**
     * Demote a user to the next lower role
     */
    @Transactional
    public boolean demoteUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Role currentRole = user.getRole();
            Role newRole = getNextLowerRole(currentRole);
            
            if (newRole != null && newRole != currentRole) {
                user.setRole(newRole);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    /**
     * Set specific role for a user
     */
    @Transactional
    public boolean setUserRole(Long userId, Role role) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(role);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * Delete any user from the system (except SUPER_ADMIN)
     */
    @Transactional
    public boolean deleteUser(Long userId, Long superAdminId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> superAdminOpt = userRepository.findById(superAdminId);
        
        if (userOpt.isPresent() && superAdminOpt.isPresent()) {
            User targetUser = userOpt.get();
            User superAdmin = superAdminOpt.get();
            
            // Only SUPER_ADMIN can delete users
            if (superAdmin.getRole() != Role.SUPER_ADMIN) {
                return false;
            }
            
            // Cannot delete another SUPER_ADMIN
            if (targetUser.getRole() == Role.SUPER_ADMIN) {
                return false;
            }
            
            // Delete associated refresh tokens first
            refreshTokenRepository.deleteByUser(targetUser);
            
            // Delete user profile if exists
            Optional<UserProfile> profileOpt = userProfileRepository.findByUser(targetUser);
            profileOpt.ifPresent(profile -> userProfileRepository.delete(profile));
            
            // Delete the user
            userRepository.delete(targetUser);
            return true;
        }
        return false;
    }

    /**
     * Create or update user profile
     */
    @Transactional
    public UserProfile createOrUpdateProfile(Long userId, String phoneNumber, String aadhaarNumber) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            Optional<UserProfile> existingProfile = userProfileRepository.findByUser(user);
            UserProfile profile;
            
            if (existingProfile.isPresent()) {
                profile = existingProfile.get();
                profile.setUpdatedAt(LocalDateTime.now());
            } else {
                profile = new UserProfile();
                profile.setUser(user);
                profile.setCreatedAt(LocalDateTime.now());
                profile.setUpdatedAt(LocalDateTime.now());
            }
            
            profile.setPhoneNumber(phoneNumber);
            profile.setAadhaarNumber(aadhaarNumber);
            
            return userProfileRepository.save(profile);
        }
        return null;
    }

    /**
     * Reset user password
     */
    @Transactional
    public boolean resetUserPassword(Long userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setFailedAttempts(0);
            user.setLocked(false);
            userRepository.save(user);
            
            // Invalidate all refresh tokens for this user
            refreshTokenRepository.deleteByUser(user);
            
            return true;
        }
        return false;
    }

    /**
     * Lock/Unlock user account
     */
    @Transactional
    public boolean toggleUserLock(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLocked(!user.isLocked());
            if (!user.isLocked()) {
                user.setFailedAttempts(0);
            }
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * Get user statistics
     */
    public UserStats getUserStats() {
        List<User> allUsers = userRepository.findAll();
        UserStats stats = new UserStats();
        
        for (User user : allUsers) {
            switch (user.getRole()) {
                case SUPER_ADMIN:
                    stats.setSuperAdmins(stats.getSuperAdmins() + 1);
                    break;
                case ADMIN:
                    stats.setAdmins(stats.getAdmins() + 1);
                    break;
                case MANAGER:
                    stats.setManagers(stats.getManagers() + 1);
                    break;
                case EMPLOYEE:
                    stats.setEmployees(stats.getEmployees() + 1);
                    break;
                case USER:
                    stats.setUsers(stats.getUsers() + 1);
                    break;
            }
            
            if (user.isActive()) {
                stats.setActiveUsers(stats.getActiveUsers() + 1);
            } else {
                stats.setInactiveUsers(stats.getInactiveUsers() + 1);
            }
            
            if (user.isLocked()) {
                stats.setLockedUsers(stats.getLockedUsers() + 1);
            }
        }
        
        stats.setTotalUsers(allUsers.size());
        return stats;
    }

    private Role getNextHigherRole(Role currentRole) {
        switch (currentRole) {
            case USER:
                return Role.EMPLOYEE;
            case EMPLOYEE:
                return Role.MANAGER;
            case MANAGER:
                return Role.ADMIN;
            case ADMIN:
                return Role.SUPER_ADMIN;
            default:
                return null;
        }
    }

    private Role getNextLowerRole(Role currentRole) {
        switch (currentRole) {
            case SUPER_ADMIN:
                return Role.ADMIN;
            case ADMIN:
                return Role.MANAGER;
            case MANAGER:
                return Role.EMPLOYEE;
            case EMPLOYEE:
                return Role.USER;
            default:
                return null;
        }
    }

    // Inner class for user statistics
    public static class UserStats {
        private int totalUsers;
        private int activeUsers;
        private int inactiveUsers;
        private int lockedUsers;
        private int superAdmins;
        private int admins;
        private int managers;
        private int employees;
        private int users;

        // Getters and setters
        public int getTotalUsers() { return totalUsers; }
        public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
        
        public int getActiveUsers() { return activeUsers; }
        public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }
        
        public int getInactiveUsers() { return inactiveUsers; }
        public void setInactiveUsers(int inactiveUsers) { this.inactiveUsers = inactiveUsers; }
        
        public int getLockedUsers() { return lockedUsers; }
        public void setLockedUsers(int lockedUsers) { this.lockedUsers = lockedUsers; }
        
        public int getSuperAdmins() { return superAdmins; }
        public void setSuperAdmins(int superAdmins) { this.superAdmins = superAdmins; }
        
        public int getAdmins() { return admins; }
        public void setAdmins(int admins) { this.admins = admins; }
        
        public int getManagers() { return managers; }
        public void setManagers(int managers) { this.managers = managers; }
        
        public int getEmployees() { return employees; }
        public void setEmployees(int employees) { this.employees = employees; }
        
        public int getUsers() { return users; }
        public void setUsers(int users) { this.users = users; }
    }
}