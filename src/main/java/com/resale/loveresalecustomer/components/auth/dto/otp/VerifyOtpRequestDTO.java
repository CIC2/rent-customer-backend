package com.resale.loveresalecustomer.components.auth.dto.otp;

import lombok.Data;

@Data
public class VerifyOtpRequestDTO {
    private String countryCode;
    private String mobile;
    private String otp;
}
