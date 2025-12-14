package com.kulebiakin.coachservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.kulebiakin.coachservice", "com.kulebiakin.common"})
public class CoachServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoachServiceApplication.class, args);
    }
}
