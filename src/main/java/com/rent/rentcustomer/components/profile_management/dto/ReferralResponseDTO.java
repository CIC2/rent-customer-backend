package com.resale.loveresalecustomer.components.profile_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReferralResponseDTO {
    private Long id;
    private String name;
    private String phoneNumber;
    private LocalDateTime createdAt;
}
