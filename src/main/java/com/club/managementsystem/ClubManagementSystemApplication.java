package com.club.managementsystem;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class ClubManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClubManagementSystemApplication.class, args);
    }
}