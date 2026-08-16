package com.piuda.careon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DolbomOnApplication {

    public static void main(String[] args) {
        SpringApplication.run(DolbomOnApplication.class, args);
    }


}
