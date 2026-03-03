package com.resale.loveresalecustomer.components.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationAppointmentDTO {
    private Integer id;
    private String zoomUrl;
    private String zoomMeetingId;
}
