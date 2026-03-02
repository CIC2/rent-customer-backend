package com.resale.loveresalecustomer.shared;

import org.springframework.stereotype.Service;

@Service
public class ProfileUpdaterService {
//    private final CustomerRepository customerRepository;

//    public CustomerProfileService(CustomerRepository customerRepository) {
//        this.customerRepository = customerRepository;
//    }

//    public Customer updateProfile(ProfileRequestDTO request) {
//        return customerRepository.findByPhoneNumber(request.getPhoneNumber())
//            .map(customer -> {
//                customer.setFullName(request.getFullName());
//                customer.setPhoneNumber(request.getPhoneNumber());
//                customer.setNationality(request.getNationality());
//                customer.setMail(request.getMail());
//                return customerRepository.save(customer);
//            })
//            .orElseThrow(() -> new CustomerNotFoundException("Customer not found with phone: " + request.getPhoneNumber()));
//    }
}