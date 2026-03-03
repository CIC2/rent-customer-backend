package com.resale.loveresalecustomer.components.profile_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HasAppointmentInfoDTO {
    Boolean hasAppointment ;
    Boolean isRateEmpty ;
    Integer appointmentId;
}
