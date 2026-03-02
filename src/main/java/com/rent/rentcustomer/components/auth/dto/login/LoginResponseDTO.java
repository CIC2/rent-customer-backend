package com.resale.loveresalecustomer.components.auth.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private String token;
    private Long customerId;
    private String fullName;
    private String mobile;
    private Boolean isVerified;
}
