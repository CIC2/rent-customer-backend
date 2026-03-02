package com.resale.loveresalecustomer.components.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFcmToken {
    private Long customerId;
    private String fcmToken;
}
