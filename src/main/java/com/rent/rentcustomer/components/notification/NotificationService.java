package com.resale.loveresalecustomer.components.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.resale.loveresalecustomer.components.notification.dto.NotificationAppointmentDTO;
import com.resale.loveresalecustomer.components.notification.dto.NotificationListDTO;
import com.resale.loveresalecustomer.components.notification.dto.NotificationWithAppointmentDTO;
import com.resale.loveresalecustomer.feign.AppointmentClient;
import com.resale.loveresalecustomer.feign.CommunicationClient;
import com.resale.loveresalecustomer.utils.PaginatedResponseDTO;
import com.resale.loveresalecustomer.utils.ReturnObject;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final CommunicationClient communicationClient;
    private final AppointmentClient appointmentClient;

    public PaginatedResponseDTO<NotificationWithAppointmentDTO> getNotificationsForCustomer(
            Long customerId, int page, int size) {

        // 1️⃣ Call Communication MS to get notifications
        ResponseEntity<PaginatedResponseDTO<NotificationListDTO>> response =
                communicationClient.getNotificationsByCustomer(customerId, page, size);

        PaginatedResponseDTO<NotificationListDTO> notificationPage = response.getBody();

        // 2️⃣ Enrich notifications with Appointment info
        List<NotificationWithAppointmentDTO> enriched = notificationPage.getContent().stream()
                .map(n -> {
                    NotificationAppointmentDTO appointment = null;
                    if (n.getTypeId() != null) {
                        try {
                            appointment = appointmentClient.getAppointment(n.getTypeId()).getBody();
                        } catch (Exception e) {
                            // If appointment not found or MS call fails, ignore
                        }
                    }

                    return new NotificationWithAppointmentDTO(
                            n.getId(),
                            n.getContent(),
                            n.getSentAt(),
                            n.getIsSeen(),
                            n.getIsOpened(),
                            n.getType(),
                            n.getTypeId(),
                            appointment != null ? appointment.getZoomUrl() : null,
                            appointment != null ? appointment.getZoomMeetingId() : null
                    );
                })
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(
                enriched,
                notificationPage.getPage(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.isLast()
        );
    }


    public ReturnObject<Void> markNotificationAsOpened(Integer notificationId, Long customerId) {
        try {
            boolean success = communicationClient.markNotificationAsOpened(notificationId, customerId);
            if (success) {
                return new ReturnObject<>( "Notification marked as opened successfully", true, null);
            } else {
                return new ReturnObject<>( "Failed to mark notification as opened", false, null);
            }
        } catch (Exception e) {
            return new ReturnObject<>( "Error: " + e.getMessage(), false,  null);
        }
    }
}

