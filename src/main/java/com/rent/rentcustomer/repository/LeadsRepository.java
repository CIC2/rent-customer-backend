package com.resale.loveresalecustomer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resale.loveresalecustomer.model.Leads;

public interface LeadsRepository extends JpaRepository<Leads, Integer> {
}
