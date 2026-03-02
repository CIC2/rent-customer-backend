package com.resale.loveresalecustomer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.resale.loveresalecustomer.components.notification.dto.NotificationListDTO;
import com.resale.loveresalecustomer.utils.PaginatedResponseDTO;

@FeignClient(
        name = "communication-ms",
        url = "${communication.ms.url}"
)
public interface CommunicationClient {
@GetMapping("/notification/customer/{customerId}")
ResponseEntity<PaginatedResponseDTO<NotificationListDTO>> getNotificationsByCustomer(
        @PathVariable Long customerId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
);

    @PostMapping("/notification/{id}/open")
    boolean markNotificationAsOpened(@PathVariable("id") Integer notificationId,
                                     @RequestParam("customerId") Long customerId);
}
