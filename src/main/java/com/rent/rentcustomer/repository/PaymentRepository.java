package com.resale.loveresalecustomer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.resale.loveresalecustomer.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer>{

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Payment p JOIN Offer o ON p.offerId = o.id " +
            "WHERE o.customerId = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);

}
