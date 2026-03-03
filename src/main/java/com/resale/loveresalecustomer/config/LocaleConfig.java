package com.resale.loveresalecustomer.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;

@Configuration
public class LocaleConfig {

    private final CustomLocaleResolver customLocaleResolver;

    public LocaleConfig(CustomLocaleResolver customLocaleResolver) {
        this.customLocaleResolver = customLocaleResolver;
    }

    @Bean
    public LocaleResolver localeResolver() {
        return customLocaleResolver;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}

