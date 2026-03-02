package com.resale.loveresalecustomer.components.profile_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseReasonResponseDTO {
    private Integer id;
    private String name;
    private List<PurchaseSubReasonResponseDTO> subReasons;
}