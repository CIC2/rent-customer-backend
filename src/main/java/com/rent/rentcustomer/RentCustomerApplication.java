package com.resale.loveresalecustomer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
@EnableRetry
public class LoveResaleCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoveResaleCustomerApplication.class, args);
    }

}
