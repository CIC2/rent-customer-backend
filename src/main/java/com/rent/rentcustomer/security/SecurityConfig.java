package com.resale.loveresalecustomer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthFilter customAuthFilter;

    public SecurityConfig(CustomAuthFilter customAuthFilter) {
        this.customAuthFilter = customAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomAuthEntryPoint authEntryPoint
                                            ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/profile/resetPassword").permitAll()
                        .requestMatchers("/profile/confirmPassword").permitAll()
                        .requestMatchers("/profile/verifyResetOtp").permitAll()
                        .requestMatchers("/profile/{id}").permitAll()
                        .requestMatchers("/profile/id").permitAll()
                        .requestMatchers("/profile/mobileAndCountryCode").permitAll()
                        .requestMatchers("/profile/listByIds").permitAll()
                        .requestMatchers("/customerClient/basicInfo").permitAll()
                        .requestMatchers("/customerClient/**").permitAll()
                        .requestMatchers("/profile/validateFullyRegistered").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                )
                .addFilterBefore(customAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    @Configuration
    public class PasswordConfig {
        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

}
