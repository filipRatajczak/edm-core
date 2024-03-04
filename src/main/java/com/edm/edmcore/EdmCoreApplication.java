package com.edm.edmcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EdmCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdmCoreApplication.class, args);
    }

}
