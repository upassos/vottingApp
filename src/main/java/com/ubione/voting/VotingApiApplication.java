package com.ubione.voting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VotingApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(VotingApiApplication.class, args);
    }
}
