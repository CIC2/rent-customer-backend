package com.resale.loveresalecustomer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_interest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInterest {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        private Long customerId;
        private Integer reasonId;
        private Integer subReasonId;
        @Column(name = "created_at", updatable = false, insertable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
