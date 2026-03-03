package com.resale.loveresalecustomer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.resale.loveresalecustomer.model.PurchaseSubReason;

import java.util.List;

@Repository
public interface PurchaseSubReasonRepository extends JpaRepository<PurchaseSubReason, Integer> {
    List<PurchaseSubReason> findAllByMainReasonId(Integer mainReasonId);
}
