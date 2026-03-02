package com.resale.loveresalecustomer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "offer")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer unitId;
    private Long customerId;
    private Integer userId;
    private Integer appointmentId;
    private String reservationAmount;
    private String unitPrice;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
    private String paymentPlan;
    private String finishing;
    private String maintenancePlan;
    private String paidAmount;
}
