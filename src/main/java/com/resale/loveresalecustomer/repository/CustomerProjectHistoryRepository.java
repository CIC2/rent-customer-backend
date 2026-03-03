package com.resale.loveresalecustomer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.resale.loveresalecustomer.model.CustomerProjectHistory;

import java.util.List;

@Repository
public interface CustomerProjectHistoryRepository extends JpaRepository<CustomerProjectHistory, Integer> {
    List<CustomerProjectHistory> findByCustomerId(Long customerId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CustomerProjectHistory c WHERE c.customerId = :customerId")
    void deleteByCustomerId(@Param("customerId") Long customerId);

}

