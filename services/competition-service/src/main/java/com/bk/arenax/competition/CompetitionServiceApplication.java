package com.bk.arenax.competition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CompetitionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompetitionServiceApplication.class, args);
    }
}
