package com.resale.loveresalecustomer.components.customer.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerBasicInfoDTO {
    private Long customerId;
    private String fullName;
    private String countryCode;
    private String mobile;
    private String nationality;
    private String email;


    public CustomerBasicInfoDTO(
            Long customerId,
            String fullName,
            String mobile
    ) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.mobile = mobile;
    }
}
