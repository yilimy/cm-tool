package com.gomain.cm.tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CmToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmToolApplication.class, args);
    }

}
