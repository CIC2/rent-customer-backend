package com.resale.loveresalecustomer.components.profile_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitCustomerTypeDTO {
    private Boolean newCustomer;
    private List<Integer> projectIds; // required if newCustomer = false
}
