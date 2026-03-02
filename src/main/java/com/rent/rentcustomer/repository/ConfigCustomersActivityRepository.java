package com.resale.loveresalecustomer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.resale.loveresalecustomer.model.ConfigCustomersActivity;

import java.util.Optional;

@Repository
public interface ConfigCustomersActivityRepository extends JpaRepository<ConfigCustomersActivity, Integer> {

    Optional<ConfigCustomersActivity> findByCustomerIdAndConfigKey(Long customerId,String key);
}