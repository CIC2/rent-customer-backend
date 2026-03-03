package com.resale.loveresalecustomer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.resale.loveresalecustomer.model.PurchaseReason;

@Repository
public interface PurchaseReasonRepository extends JpaRepository<PurchaseReason, Integer> {

}
