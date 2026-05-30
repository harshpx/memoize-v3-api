package com.memoize.api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MemoizeV3ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemoizeV3ApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner cmdLineRunner() {
        return run -> {
            System.out.println("Memoize V3 API is running!");
        };
    }
}
