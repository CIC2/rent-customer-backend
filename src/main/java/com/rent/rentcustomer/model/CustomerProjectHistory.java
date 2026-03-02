package com.resale.loveresalecustomer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_project_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProjectHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @Column(name = "new_customer")
    private Boolean newCustomer;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
