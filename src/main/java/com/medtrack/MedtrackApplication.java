package com.medtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedtrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedtrackApplication.class, args);
    }
}
