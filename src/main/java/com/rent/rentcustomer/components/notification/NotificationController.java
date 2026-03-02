package com.resale.loveresalecustomer.components.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.resale.loveresalecustomer.components.notification.dto.NotificationWithAppointmentDTO;
import com.resale.loveresalecustomer.security.CustomUserPrincipal;
import com.resale.loveresalecustomer.utils.PaginatedResponseDTO;
import com.resale.loveresalecustomer.utils.ReturnObject;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<NotificationWithAppointmentDTO>> getNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long customerId = principal.getId();
        return ResponseEntity.ok(
                notificationService.getNotificationsForCustomer(customerId, page, size)
        );
    }


    @PostMapping("/open/{id}")
    public ResponseEntity<ReturnObject<Void>> openNotification(
            @PathVariable("id") Integer notificationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long customerId = principal.getId();
        ReturnObject<Void> result = notificationService.markNotificationAsOpened(notificationId, customerId);

        HttpStatus status = result.getStatus() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(result, status);
    }
}
