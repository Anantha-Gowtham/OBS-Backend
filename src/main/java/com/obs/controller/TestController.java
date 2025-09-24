package com.obs.controller;

import com.obs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public Map<String, Object> testRegister(@RequestBody Map<String, Object> request) {
        System.out.println("Test registration received: " + request);
        return Map.of("success", true, "message", "Test registration received", "data", request);
    }

    @GetMapping("/simple")
    public Map<String, Object> simpleTest() {
        return Map.of(
            "message", "Simple test endpoint working",
            "timestamp", System.currentTimeMillis(),
            "status", "OK"
        );
    }

    @GetMapping("/users")
    public Map<String, Object> getUserCount() {
        long userCount = userRepository.count();
        boolean hasAdmin = userRepository.findByUsername("admin").isPresent();
        return Map.of(
            "userCount", userCount,
            "hasAdmin", hasAdmin,
            "message", "User database status"
        );
    }
}
