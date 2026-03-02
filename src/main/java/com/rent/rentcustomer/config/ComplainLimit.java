package com.resale.loveresalecustomer.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ComplainLimit {

    @Value("${complain.limit.count}")
    private int limitCount;

    @Value("${complain.limit.duration-minutes}")
    private int durationMinutes;
}
