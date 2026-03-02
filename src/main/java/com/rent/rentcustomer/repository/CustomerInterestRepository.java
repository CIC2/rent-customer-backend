package com.resale.loveresalecustomer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.resale.loveresalecustomer.model.CustomerInterest;

import java.util.List;

@Repository
public interface CustomerInterestRepository extends JpaRepository<CustomerInterest, Long>{
    List<CustomerInterest> findAllByCustomerId(Long customerId);
}
