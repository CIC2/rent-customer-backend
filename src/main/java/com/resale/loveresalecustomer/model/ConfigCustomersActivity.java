package com.resale.loveresalecustomer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "config_customers_activity", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"customerId", "configKey"})
})
public class ConfigCustomersActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private String configKey;
    private LocalDateTime lastShownAt;
}