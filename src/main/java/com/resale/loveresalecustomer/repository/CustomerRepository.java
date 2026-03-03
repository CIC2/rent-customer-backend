package com.resale.loveresalecustomer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.resale.loveresalecustomer.components.customer.dto.CustomerFcmToken;
import com.resale.loveresalecustomer.components.customer.dto.CustomerPhoneDTO;
import com.resale.loveresalecustomer.model.Customer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByMobile(String mobile);
    Optional<Customer> findByMobile(String mobile);
    Optional<Customer> findByMobileOrEmail(String mobile, String email);
    boolean existsByEmail(String email);
    boolean existsByNationalId(String nationalId);
    boolean existsByPassportNumber (String passportNumber);
    Optional<Customer> findByCountryCodeAndMobile(String countryCode, String mobile);
    boolean existsByCountryCodeAndMobile(String countryCode, String mobile);


    List<Customer> findAllByIdIn(List<Integer> ids);

    @Query("SELECT c FROM Customer c WHERE c.id IN :ids " +
            "AND (:name IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "   OR LOWER(c.arabicFullName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:mobile IS NULL OR c.mobile LIKE CONCAT('%', :mobile, '%'))")
    List<Customer> findAllByIdInAndOptionalFilters(@Param("ids") List<Integer> ids,
                                                   @Param("name") String name,
                                                   @Param("mobile") String mobile);

    @Query("""
    SELECT new com.resale.loveresalecustomer.components.customer.dto.CustomerPhoneDTO(c.id, c.mobile)
    FROM Customer c
     where c.countryCode = '+20'
          and c.mobile is not null
          and c.isBlocked = false""")
    List<CustomerPhoneDTO> findEgyptianCustomersWithPhone();

    @Query("SELECT c.id AS customerId, c.email AS email FROM Customer c WHERE c.email IS NOT NULL")
    List<Map<String, Object>> findAllCustomerEmails();

    @Query("SELECT new com.resale.loveresalecustomer.components.customer.dto.CustomerFcmToken(c.id, c.fcmToken) " +
            "FROM Customer c WHERE c.fcmToken IS NOT NULL")
    List<CustomerFcmToken> findAllWithFcmTokens();
}