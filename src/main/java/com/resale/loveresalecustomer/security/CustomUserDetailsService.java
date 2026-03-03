package com.resale.loveresalecustomer.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.resale.loveresalecustomer.model.Customer;
import com.resale.loveresalecustomer.repository.CustomerRepository;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String mobile) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByMobile(mobile)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with mobile: " + mobile));

        if (customer.getPassword() == null || customer.getPassword().isEmpty()) {
            throw new UsernameNotFoundException("User has no password set: " + mobile);
        }

        return new CustomUserPrincipal(customer);
    }

}
