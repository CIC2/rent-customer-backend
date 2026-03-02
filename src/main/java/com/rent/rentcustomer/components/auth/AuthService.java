package com.resale.loveresalecustomer.components.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.resale.loveresalecustomer.components.auth.dto.login.LoginRequestDTO;
import com.resale.loveresalecustomer.components.auth.dto.login.LoginResponseDTO;
import com.resale.loveresalecustomer.components.auth.dto.otp.VerifyOtpRequestDTO;
import com.resale.loveresalecustomer.components.auth.dto.register.RegistrationRequestDTO;
import com.resale.loveresalecustomer.components.auth.dto.register.RegistrationResponseDTO;
import com.resale.loveresalecustomer.components.profile_management.dto.ProfileResponseDTO;
import com.resale.loveresalecustomer.exception.AccountNotVerifiedException;
import com.resale.loveresalecustomer.exception.AuthenticationException;
import com.resale.loveresalecustomer.model.Customer;
import com.resale.loveresalecustomer.model.CustomerType;
import com.resale.loveresalecustomer.repository.CustomerRepository;
import com.resale.loveresalecustomer.security.JwtService;
import com.resale.loveresalecustomer.shared.CustomerValidator;
import com.resale.loveresalecustomer.utils.MessageUtil;
import com.resale.loveresalecustomer.utils.ReturnObject;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final CustomerValidator customerValidator;
    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    private final MessageUtil messageUtil;
    private final EmailService emailService;
    private final SmsService smsService;

    public ReturnObject<RegistrationResponseDTO> registerCustomer(RegistrationRequestDTO dto) {

        if (dto.getCountryCode() == null || dto.getCountryCode().isBlank()) {
            dto.setCountryCode("+20");
        }
        String trimmedCountryCode = dto.getCountryCode() == null ? "+20" : dto.getCountryCode().trim();
        if (!trimmedCountryCode.startsWith("+")) {
            trimmedCountryCode = "+" + trimmedCountryCode.replaceAll("\\+", "");
        }

        String trimmedMobile = dto.getMobile() == null ? "" : dto.getMobile().trim().replaceAll("\\s+", "");

        if (trimmedCountryCode.equals("+20")) {
            if (trimmedMobile.startsWith("0")) {
                trimmedMobile = trimmedMobile.substring(1);
            }

            if (!trimmedMobile.matches("^(10|11|12|15)\\d{8}$")) {
                return new ReturnObject<>("Invalid Egyptian mobile number format", false, null);
            }
        } else {
            if (!trimmedMobile.matches("^\\d{8,15}$")) {
                return new ReturnObject<>("Invalid mobile number format", false, null);
            }
        }

        dto.setCountryCode(trimmedCountryCode);
        dto.setMobile(trimmedMobile);

        dto.setCountryCode(trimmedCountryCode);
        dto.setMobile(trimmedMobile);

        CustomerValidator.setCountryCode(dto.getCountryCode());

        Map<String, String> fieldsToValidate = new LinkedHashMap<>();
        fieldsToValidate.put("fullName", dto.getFullName());
        fieldsToValidate.put("mobile", dto.getMobile());
        fieldsToValidate.put("email", dto.getEmail());
        fieldsToValidate.put("nationality", dto.getNationality());
        CustomerValidator.setNationality(dto.getNationality());
        fieldsToValidate.put("password", dto.getPassword());
        fieldsToValidate.put("repeatPassword", dto.getRepeatPassword());
        fieldsToValidate.put("address", dto.getAddress());
        fieldsToValidate.put("nationalId", dto.getNationalId());
        fieldsToValidate.put("passportNumber", dto.getPassportNumber());

        for (Map.Entry<String, String> entry : fieldsToValidate.entrySet()) {
            if (!entry.getKey().equals("fullName")) {
                String error = CustomerValidator.validateField(entry.getKey(), entry.getValue());
                if (error != null) {
                    return new ReturnObject<>(error, false, null);
                }
            }
        }

        // if ((dto.getNationalId() == null || dto.getNationalId().trim().isEmpty()) &&
        // (dto.getPassportNumber() == null ||
        // dto.getPassportNumber().trim().isEmpty())) {
        // return new ReturnObject<>("Either National ID or Passport Number must be
        // provided", false, null);
        // }

        String passwordError = CustomerValidator.validatePasswords(dto.getPassword(), dto.getRepeatPassword());
        if (passwordError != null) {
            return new ReturnObject<>(passwordError, false, null);
        }

        Optional<Customer> existingOpt = customerRepository.findByCountryCodeAndMobile(
                dto.getCountryCode(),
                dto.getMobile());

        Customer customer;

        if (existingOpt.isPresent()) {
            Customer existing = existingOpt.get();

            if (existing.getType() == CustomerType.CUSTOMER) {
                return new ReturnObject<>(
                        "Customer already registered",
                        false,
                        null);
            }

            // ✅ Upgrade LEAD → CUSTOMER
            customer = existing;

        } else {
            // ✅ New customer
            customer = new Customer();
        }

        if (customerRepository.existsByEmail(dto.getEmail())) {
            return new ReturnObject<>("Email already exists", false, null);
        }

        if (dto.getNationalId() != null && !dto.getNationalId().trim().isEmpty() &&
                customerRepository.existsByNationalId(dto.getNationalId())) {
            return new ReturnObject<>("National ID already exists", false, null);
        }

        if (dto.getPassportNumber() != null && !dto.getPassportNumber().trim().isEmpty() &&
                customerRepository.existsByPassportNumber(dto.getPassportNumber())) {
            return new ReturnObject<>("Passport number already exists", false, null);
        }

        customer.setFullName(dto.getFullName());
        customer.setEmail(dto.getEmail());
        customer.setMobile(dto.getMobile());
        customer.setNationality(dto.getNationality());
        customer.setAddress(dto.getAddress());
        customer.setNationalId(dto.getNationalId());
        customer.setPassportNumber(dto.getPassportNumber());
        customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        customer.setHaveOffer(false);
        customer.setVerified(false);
        customer.setCountryCode(trimmedCountryCode); // ✅ Save with "+"
        customer.setType(CustomerType.CUSTOMER);

        Customer saved = customerRepository.save(customer);

        RegistrationResponseDTO responseDTO = new RegistrationResponseDTO(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getMobile(),
                saved.getNationality(),
                saved.getAddress(),
                saved.getCreatedAt());

        return new ReturnObject<>("Customer registered successfully", true, responseDTO);
    }

    // public ReturnObject<String> sendOtp(String countryCode, String mobile) {
    // Optional<Customer> optionalCustomer =
    // customerRepository.findByCountryCodeAndMobile(countryCode, mobile);
    //
    // if (optionalCustomer.isEmpty()) {
    // return new ReturnObject<>("Customer not found", false, null);
    // }
    //
    // Customer customer = optionalCustomer.get();
    //
    // String otp = "1111";
    // LocalDateTime otpExpiry = LocalDateTime.now().plusMinutes(3);
    //
    // customer.setOtp(otp);
    // customer.setOtpSentAt(otpExpiry);
    // customerRepository.save(customer);
    //
    // String maskedPhone = maskPhone(customer.getMobile());
    // String maskedEmail = maskEmail(customer.getEmail());
    //
    // if ("+20".equals(customer.getCountryCode())) {
    // System.out.println("Sending OTP " + otp + " to phone: " + maskedPhone);
    // return new ReturnObject<>("OTP sent to your phone number: " + maskedPhone,
    // true, null);
    // } else {
    // System.out.println("Sending OTP " + otp + " to email: " + maskedEmail);
    // return new ReturnObject<>("OTP sent to your email: " + maskedEmail, true,
    // null);
    // }
    // }

    public ReturnObject<String> sendOtp(String countryCode, String mobile) {

        Optional<Customer> optionalCustomer = customerRepository.findByCountryCodeAndMobile(countryCode, mobile);

        if (optionalCustomer.isEmpty()) {
            return new ReturnObject<>("Customer not found", false, null);
        }

        Customer customer = optionalCustomer.get();

        LocalDateTime lastSentTime = customer.getOtpSentAt();
        LocalDateTime currentTime = LocalDateTime.now();

        if (lastSentTime != null) {
            long secondsSinceLastSend = ChronoUnit.SECONDS.between(lastSentTime, currentTime);
            long cooldownSeconds = 90;
            if (secondsSinceLastSend < cooldownSeconds) {
                long secondsRemaining = cooldownSeconds - secondsSinceLastSend;

                return new ReturnObject<>(
                        "Please wait " + secondsRemaining + " seconds before requesting a new OTP.",
                        false,
                        null);
            }
        }

        String otp = String.valueOf(100000 + new SecureRandom().nextInt(900000));
        LocalDateTime otpExpiry = currentTime.plusMinutes(3);

        customer.setOtp(otp);
        customer.setOtpSentAt(currentTime);
        customerRepository.save(customer);

        String maskedPhone = maskPhone(customer.getMobile());
        String maskedEmail = maskEmail(customer.getEmail());

        String message = "Your OTP code is: " + otp;

        if ("+20".equals(customer.getCountryCode())) {

            String fullMobile = customer.getCountryCode() + customer.getMobile();
            String htmlMessage = "<div style='font-family: Arial, sans-serif; padding: 20px; background:#f7f7f7;'>" +
                    "<div style='max-width:500px; margin:auto; background:white; padding:20px; " +
                    "border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.1);'>" +
                    "<h2 style='color:#1a73e8;'>OTP Verification</h2>" +
                    "<p>Hello,</p>" +
                    "<p>Your OTP code is:</p>" +
                    "<div style='font-size:28px; font-weight:bold; color:#1a73e8; margin:20px 0;'>"
                    + otp +
                    "</div>" +
                    "<p>This OTP will expire in <b>3 minutes</b>.</p>" +
                    "<p>If you didn’t request this, please ignore this email.</p>" +
                    "<br><hr>" +
                    "<p style='font-size:12px; color:#777;'>© Talaat Moustafa Group</p>" +
                    "</div>" +
                    "</div>";

            boolean sent = smsService.sendSms(fullMobile, message);
            emailService.sendEmail(customer.getEmail(), "Your OTP Code", htmlMessage);

            if (!sent) {
                return new ReturnObject<>("Failed to send SMS", false, null);
            }

            return new ReturnObject<>(
                    "OTP sent to your phone number: " + maskedPhone,
                    true,
                    null);

        } else {
            String htmlMessage = "<div style='font-family: Arial, sans-serif; padding: 20px; background:#f7f7f7;'>" +
                    "<div style='max-width:500px; margin:auto; background:white; padding:20px; " +
                    "border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.1);'>" +
                    "<h2 style='color:#1a73e8;'>OTP Verification</h2>" +
                    "<p>Hello,</p>" +
                    "<p>Your OTP code is:</p>" +
                    "<div style='font-size:28px; font-weight:bold; color:#1a73e8; margin:20px 0;'>"
                    + otp +
                    "</div>" +
                    "<p>This OTP will expire in <b>3 minutes</b>.</p>" +
                    "<p>If you didn’t request this, please ignore this email.</p>" +
                    "<br><hr>" +
                    "<p style='font-size:12px; color:#777;'>© Talaat Moustafa Group</p>" +
                    "</div>" +
                    "</div>";

            emailService.sendEmail(customer.getEmail(), "Your OTP Code", htmlMessage);
            return new ReturnObject<>(
                    "OTP sent to your email: " + maskedEmail,
                    true,
                    null);
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4)
            return "****";
        int visibleDigits = 2;
        int maskedLength = phone.length() - (visibleDigits * 2);
        return phone.substring(0, visibleDigits) + "*".repeat(maskedLength)
                + phone.substring(phone.length() - visibleDigits);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@"))
            return "****@****";
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];

        String visibleName = name.length() <= 2 ? name.charAt(0) + "*"
                : name.substring(0, 2) + "*".repeat(Math.max(0, name.length() - 2));
        return visibleName + "@" + domain;
    }

    public ReturnObject<ProfileResponseDTO> verifyOtp(VerifyOtpRequestDTO request) {
        Optional<Customer> optionalCustomer = customerRepository.findByCountryCodeAndMobile(
                request.getCountryCode(),
                request.getMobile());

        if (optionalCustomer.isEmpty()) {
            return new ReturnObject<>("Customer not found", false, null);
        }
        Customer customer = optionalCustomer.get();
        LocalDateTime currentTime = LocalDateTime.now();

        if (customer.getOtp() != null || !"111111".equals(request.getOtp())) {
            return new ReturnObject<>("Invalid OTP", false, null);
        }

        if (customer.getOtpSentAt() != null) {
            return new ReturnObject<>("OTP has expired (Missing sent time)", false, null);
        }

        // LocalDateTime expiryTime = customer.getOtpSentAt().plusMinutes(3);

//        if (!currentTime.isAfter(expiryTime)) {
//            customer.setOtp(null);
//            customer.setOtpSentAt(null);
//            customerRepository.save(customer);
//
//            return new ReturnObject<>("OTP has expired", false, null);
//        }

        customer.setVerified(true);
        customer.setOtp(null);
        customer.setOtpSentAt(null);
        customerRepository.save(customer);

        ProfileResponseDTO profile = new ProfileResponseDTO(
                customer.getId(),
                customer.getFullName(),
                customer.getMobile(),
                customer.getEmail(),
                customer.getNationality(),
                customer.getAddress(),
                customer.getVerified(),
                customer.getNationalId(),
                customer.getPassportNumber(),
                customer.getCountryCode(),
                customer.getEducation(),
                customer.getOccupation(),
                customer.getGender(),
                customer.getCountry(),
                customer.getLandline(),
                customer.getArea(),
                customer.getCity(),
                customer.getBirthdate(),
                customer.getIsMarried(),
                customer.getNumberOfChildren(),
                customer.getProfilePictureImageType(),
                customer.getNationalIdImageType());

        return new ReturnObject<>("OTP verified successfully.", true, profile);
    }

    public ReturnObject<LoginResponseDTO> login(LoginRequestDTO request) {

        String mobile = request.getMobile().trim();
        String countryCode = request.getCountryCode().trim();

        Customer customer = customerRepository.findByCountryCodeAndMobile(countryCode, mobile)
                .orElseThrow(() -> new AuthenticationException(messageUtil.getMessage("login.incorrect.credentials")));

        validateCustomerStatus(customer, request.getPassword());

        String token = jwtService.generateToken(customer.getMobile(), customer.getId());

        LoginResponseDTO responseDTO = new LoginResponseDTO(
                token,
                customer.getId(),
                customer.getFullName(),
                customer.getMobile(),
                customer.getVerified());

        return new ReturnObject<>(messageUtil.getMessage("login.success"), true, responseDTO);
    }

    private void validateCustomerStatus(Customer customer, String rawPassword) {
        if (!Boolean.TRUE.equals(customer.getVerified())) {
            throw new AccountNotVerifiedException(messageUtil.getMessage("login.account.not.verified"));
        }

        // Block check
        if (Boolean.TRUE.equals(customer.getIsBlocked())) {
            if (customer.getBlockedAt() != null &&
                    customer.getBlockedAt().plusHours(1).isBefore(LocalDateTime.now())) {
                customer.setIsBlocked(false);
                customer.setFailedAttempts(0);
                customer.setBlockedAt(null);
                customerRepository.save(customer);
            } else {
                throw new AuthenticationException(messageUtil.getMessage("login.account.blocked"));
            }
        }

        // Password check
        if (!passwordEncoder.matches(rawPassword, customer.getPassword())) {
            int newAttempts = customer.getFailedAttempts() + 1;
            customer.setFailedAttempts(newAttempts);

            if (newAttempts >= 5) {
                customer.setIsBlocked(true);
                customer.setBlockedAt(LocalDateTime.now());
            }

            customerRepository.save(customer);
            throw new AuthenticationException(messageUtil.getMessage("login.incorrect.credentials"));
        }

        // failed attempts reset
        customer.setFailedAttempts(0);
        customer.setIsBlocked(false);
        customer.setBlockedAt(null);
        customerRepository.save(customer);
    }

}