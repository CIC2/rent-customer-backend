package com.resale.loveresalecustomer.components.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.resale.loveresalecustomer.model.NotificationType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationListDTO {
    private Integer id;
    private String content;
    private LocalDateTime sentAt;
    private Boolean isSeen;
    private Boolean isOpened;
    private NotificationType type;
    private Integer typeId;
}
