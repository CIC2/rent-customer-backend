package com.resale.loveresalecustomer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.resale.loveresalecustomer.components.notification.dto.NotificationAppointmentDTO;
import com.resale.loveresalecustomer.components.profile_management.dto.HasAppointmentInfoDTO;
import com.resale.loveresalecustomer.utils.ReturnObject;

@FeignClient(name = "appointment-ms", url = "${appointment.ms.url}")
public interface AppointmentClient {

    @GetMapping("/customer/{appointmentId}")
    ResponseEntity<NotificationAppointmentDTO> getAppointment(@PathVariable Integer appointmentId);
    @GetMapping("/customer/customerOnCall/{customerId}")
    ResponseEntity<ReturnObject<HasAppointmentInfoDTO>> getAppointmentOnCall(@PathVariable Long customerId);
}
