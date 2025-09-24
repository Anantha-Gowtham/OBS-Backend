package com.obs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OnlineBankingSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnlineBankingSystemApplication.class, args);
    }
}
