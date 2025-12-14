package com.kulebiakin.sessionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.kulebiakin.sessionservice", "com.kulebiakin.common"})
public class SessionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SessionServiceApplication.class, args);
    }
}
