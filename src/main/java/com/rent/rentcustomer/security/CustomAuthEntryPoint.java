package com.resale.loveresalecustomer.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resale.loveresalecustomer.utils.ReturnObject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ReturnObject<Object> body = new ReturnObject<>(
                "Unauthorized: Please login to access this resource",
                false,
                null
        );

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}
