package com.resale.loveresalecustomer.components.auth.dto.register;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String mobile;
    private String nationality;
    private String address;
    private LocalDateTime createdAt;
}

