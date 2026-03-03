package com.resale.loveresalecustomer.components.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.resale.loveresalecustomer.components.customer.dto.CustomerBasicInfoDTO;
import com.resale.loveresalecustomer.components.customer.dto.CustomerFcmToken;
import com.resale.loveresalecustomer.utils.ReturnObject;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customerClient")
@RequiredArgsConstructor
public class InternalController {

    private final InternalService customerService;

    @GetMapping("/egyptianNumbers")
    public ResponseEntity<ReturnObject> getEgyptianCustomerPhoneNumbers() {
        ReturnObject response = customerService.getEgyptianCustomerPhonesWithIds();
        return response.getStatus()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


    @GetMapping("/emails")
    public ResponseEntity<?> getCustomerEmails() {
        ReturnObject<List<Map<String, Object>>> result = customerService.getAllCustomerEmails();

        if (!result.getStatus()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(result);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }


    @GetMapping("/{customerId}")
    public ResponseEntity<ReturnObject<CustomerBasicInfoDTO>> getCustomerBasicInfo(
            @PathVariable Long customerId) {

        ReturnObject<CustomerBasicInfoDTO> response =
                customerService.getCustomerBasicInfo(customerId);

        if (Boolean.FALSE.equals(response.getStatus())) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("/batch/basicInfo")
    public ResponseEntity<ReturnObject<List<CustomerBasicInfoDTO>>> getCustomersBasicInfo(@RequestBody List<Long> customerIds)

    {
        ReturnObject<List<CustomerBasicInfoDTO>> response =
                customerService.getCustomersBasicInfo(customerIds);

        if (Boolean.FALSE.equals(response.getStatus())) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/fcmTokensWithIds")
    public ResponseEntity<ReturnObject<List<CustomerFcmToken>>> getAllFcmTokensWithIds() {
        ReturnObject<List<CustomerFcmToken>> response = customerService.getAllFcmTokensWithIds();
        if (!response.getStatus()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }
}


