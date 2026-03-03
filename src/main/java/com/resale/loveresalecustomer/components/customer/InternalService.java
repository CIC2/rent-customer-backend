package com.resale.loveresalecustomer.components.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resale.loveresalecustomer.components.customer.dto.CustomerBasicInfoDTO;
import com.resale.loveresalecustomer.components.customer.dto.CustomerFcmToken;
import com.resale.loveresalecustomer.components.customer.dto.CustomerPhoneDTO;
import com.resale.loveresalecustomer.model.Customer;
import com.resale.loveresalecustomer.repository.CustomerRepository;
import com.resale.loveresalecustomer.utils.ReturnObject;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InternalService {

    private final CustomerRepository customerRepository;

    public ReturnObject getEgyptianCustomerPhonesWithIds() {

        ReturnObject ro = new ReturnObject();

        List<CustomerPhoneDTO> data =
                customerRepository.findEgyptianCustomersWithPhone();

        if (data.isEmpty()) {
            ro.setStatus(false);
            ro.setMessage("No Egyptian customers found");
            ro.setData(null);
            return ro;
        }

        ro.setStatus(true);
        ro.setMessage("Egyptian customers retrieved successfully");
        ro.setData(data);
        return ro;
    }


    public ReturnObject<List<Map<String, Object>>> getAllCustomerEmails() {
        ReturnObject<List<Map<String, Object>>> returnObject = new ReturnObject<>();

        try {
            List<Map<String, Object>> emails = customerRepository.findAllCustomerEmails();

            returnObject.setStatus(true);
            returnObject.setMessage("Customer emails retrieved successfully");
            returnObject.setData(emails);

        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setStatus(false);
            returnObject.setMessage("Failed to fetch customer emails: " + e.getMessage());
        }

        return returnObject;
    }


    public ReturnObject<CustomerBasicInfoDTO> getCustomerBasicInfo(Long customerId) {

        Customer customer = customerRepository.findById(customerId).orElse(null);

        if (customer == null) {
            return new ReturnObject<>(
                    "Customer not found",
                    false,
                    null
            );
        }

        CustomerBasicInfoDTO dto = new CustomerBasicInfoDTO(
                customer.getId(),
                customer.getFullName(),
                customer.getCountryCode(),
                customer.getMobile(),
                customer.getNationality(),
                customer.getEmail()
        );

        return new ReturnObject<>(
                "Customer basic info retrieved successfully",
                true,
                dto
        );
    }


    @Transactional(readOnly = true)
    public ReturnObject<List<CustomerBasicInfoDTO>> getCustomersBasicInfo(
            List<Long> customerIds
    ) {
        if (customerIds == null || customerIds.isEmpty()) {
            return new ReturnObject<>(
                    "Customer IDs list is empty",
                    false,
                    List.of()
            );
        }

        List<Customer> customers = customerRepository.findAllById(customerIds);

        if (customers.isEmpty()) {
            return new ReturnObject<>(
                    "No customers found",
                    false,
                    List.of()
            );
        }

        List<CustomerBasicInfoDTO> result = customers.stream()
                .map(c -> new CustomerBasicInfoDTO(
                        c.getId(),
                        c.getFullName(),
                        c.getMobile()
                ))
                .toList();

        return new ReturnObject<>(
                "Customers basic info retrieved successfully",
                true,
                result
        );
    }


    public ReturnObject<List<CustomerFcmToken>> getAllFcmTokensWithIds() {
        List<CustomerFcmToken> tokens = customerRepository.findAllWithFcmTokens();

        if (tokens.isEmpty()) {
            return new ReturnObject<>("No FCM tokens available", false, null);
        }

        return new ReturnObject<>("FCM tokens retrieved successfully", true, tokens);
    }
}