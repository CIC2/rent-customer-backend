package com.resale.loveresalecustomer.components.profile_management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddReferralDTO {
    @NotBlank(message = "Referral name is required")
    private String name;

//    @NotBlank (message = "Country Code is required")
//    private String countryCode;

    @NotBlank(message = "Referral phone number is required")
    private String mobile;

    private String countryCode;
}
