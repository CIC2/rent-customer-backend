package com.resale.loveresalecustomer.components.profile_management;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.resale.loveresalecustomer.components.auth.EmailService;
import com.resale.loveresalecustomer.components.auth.SmsService;
import com.resale.loveresalecustomer.components.profile_management.dto.*;
import com.resale.loveresalecustomer.config.ComplainLimit;
import com.resale.loveresalecustomer.feign.AppointmentClient;
import com.resale.loveresalecustomer.model.*;
import com.resale.loveresalecustomer.repository.*;
import com.resale.loveresalecustomer.security.CustomUserPrincipal;
import com.resale.loveresalecustomer.shared.ConfigurationService;
import com.resale.loveresalecustomer.shared.CustomerValidator;
import com.resale.loveresalecustomer.utils.MessageUtil;
import com.resale.loveresalecustomer.utils.ReturnObject;

import static com.resale.loveresalecustomer.shared.CustomerValidator.*;
import static com.resale.loveresalecustomer.utils.DateUtils.parseFlexibleDate;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ProfileService {

    @Autowired
    ComplainRepository complainRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    LeadsRepository leadsRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    private CustomerInterestRepository customerInterestRepository;
    @Autowired
    private CustomerProjectHistoryRepository customerProjectHistoryRepository;
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    ComplainLimit complainLimit;
    @Autowired
    private MessageUtil messageUtil;
    @Autowired
    PurchaseReasonRepository purchaseReasonRepository;
    @Autowired
    PurchaseSubReasonRepository purchaseSubReasonRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    SmsService smsService;
    @Autowired
    AppointmentClient appointmentClient;
    private static final double MAX_PERCENTAGE = 100.0;

    public ReturnObject<ProfileResponseDTO> getMyProfile(String token, Long customerId) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            return new ReturnObject<>("Customer not found", false, null);
        }

        Customer existing = customerOpt.get();

        double percentage = calculateProfileCompletionStatic(existing);
        String formattedPercentage = String.format("%.0f", percentage);

        List<CustomerProjectHistory> histories = customerProjectHistoryRepository.findByCustomerId(customerId);
        Boolean newCustomer = null;
        if (!histories.isEmpty()) {
            CustomerProjectHistory latestHistory = histories.stream()
                    .max(Comparator.comparing(CustomerProjectHistory::getCreatedAt))
                    .orElse(null);

            if (latestHistory != null) {
                newCustomer = latestHistory.getNewCustomer();
            }
        }

        List<Integer> projectIds = histories.stream()
                .map(CustomerProjectHistory::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        ProfileResponseDTO dto = new ProfileResponseDTO(
                token,
                existing.getId(),
                existing.getFullName(),
                existing.getMobile(),
                existing.getEmail(),
                existing.getNationality(),
                existing.getAddress(),
                existing.getVerified(),
                existing.getNationalId(),
                existing.getPassportNumber(),
                existing.getCountryCode(),
                existing.getEducation(),
                existing.getOccupation(),
                existing.getGender(),
                existing.getCountry(),
                existing.getLandline(),
                existing.getArea(),
                existing.getCity(),
                existing.getBirthdate(),
                existing.getIsMarried(),
                existing.getNumberOfChildren(),
                formattedPercentage,
                existing.getProfilePictureImageType(),
                existing.getNationalIdImageType());
        dto.setFcmToken(existing.getFcmToken());
        dto.setNewCustomer(newCustomer);
        dto.setProjectIds(projectIds);

        try {
            ResponseEntity<ReturnObject<HasAppointmentInfoDTO>> hasAppointmentResponse = appointmentClient
                    .getAppointmentOnCall(customerId);

            if (hasAppointmentResponse.getStatusCode().is2xxSuccessful()
                    && hasAppointmentResponse.getBody() != null
                    && hasAppointmentResponse.getBody().getStatus()) {
                HasAppointmentInfoDTO hasAppointmentInfoDTO = hasAppointmentResponse.getBody().getData();
                if (hasAppointmentInfoDTO.getHasAppointment()) {
                    dto.setHasActiveCall(true);
                    dto.setAppointmentId(hasAppointmentInfoDTO.getAppointmentId());
                    dto.setHasAppointmentToRate(false);
                } else if (hasAppointmentInfoDTO.getIsRateEmpty()) {
                    dto.setHasAppointmentToRate(true);
                    dto.setHasActiveCall(false);
                    dto.setAppointmentId(null);
                } else {
                    dto.setHasActiveCall(false);
                    dto.setHasAppointmentToRate(false);
                    dto.setAppointmentId(null);
                }
            }

        } catch (Exception ex) {
            dto.setHasActiveCall(false);
            dto.setHasAppointmentToRate(false);
            dto.setAppointmentId(null);
        }
        if (Integer.parseInt(formattedPercentage) >= 100) {
            dto.setShowPopup(false);
            dto.setPopupContent(null);
        } else {
            // Apply your existing logic (only if percentage > 100)
            configurationService.getActiveConfig("profile_popup").ifPresent(config -> {
                if (configurationService.shouldShowConfig(Long.valueOf(customerId), config.getConfigKey())) {
                    dto.setShowPopup(true);
                    dto.setPopupContent(config.getConditionValue());
                    dto.setPopupTitle(config.getConditionType());
                    configurationService.markConfigShown(Long.valueOf(customerId), config.getConfigKey());
                } else {
                    dto.setShowPopup(false);
                    dto.setPopupContent(null);
                }
            });
            configurationService.getActiveConfig("purchase_reason").ifPresent(config -> {
                dto.setActivePurchaseReason(config.getIsActive());
            });
        }
        return new ReturnObject<>("Profile retrieved successfully", true, dto);
    }

    public ReturnObject<ProfileResponseDTO> getProfile(String token, Long customerId) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        ArrayList<CustomerByIdPurchaseReasonDTO> customerByIdPurchaseReasonDTOArrayList = new ArrayList<>();
        if (customerOpt.isEmpty()) {
            return new ReturnObject<>("Customer not found", false, null);
        }

        Customer existing = customerOpt.get();

        double percentage = calculateProfileCompletionStatic(existing);
        String formattedPercentage = String.format("%.0f", percentage);

        List<CustomerProjectHistory> histories = customerProjectHistoryRepository.findByCustomerId(customerId);
        List<CustomerInterest> customerInterestList = customerInterestRepository.findAllByCustomerId(customerId);
        if (!customerInterestList.isEmpty()) {
            for (CustomerInterest customerInterest : customerInterestList) {
                if (customerInterest.getReasonId() != null) {
                    Optional<PurchaseReason> purchaseReasonOptional = purchaseReasonRepository
                            .findById(customerInterest.getReasonId());
                    if (purchaseReasonOptional.isPresent()) {
                        PurchaseReason purchaseReason = purchaseReasonOptional.get();
                        CustomerByIdPurchaseReasonDTO customerByIdPurchaseReasonDTO = new CustomerByIdPurchaseReasonDTO();
                        customerByIdPurchaseReasonDTO.setId(purchaseReason.getId());
                        customerByIdPurchaseReasonDTO.setNameEn(purchaseReason.getNameEn());
                        customerByIdPurchaseReasonDTO.setNameAr(purchaseReason.getNameAr());
                        customerByIdPurchaseReasonDTO.setCreatedAt(customerInterest.getCreatedAt());
                        if (customerInterest.getSubReasonId() != null) {
                            Optional<PurchaseSubReason> purchaseSubReasonOptional = purchaseSubReasonRepository
                                    .findById(customerInterest.getSubReasonId());
                            if (purchaseSubReasonOptional.isPresent()) {
                                PurchaseSubReason purchaseSubReason = purchaseSubReasonOptional.get();
                                CustomerByIdPurchaseSubReasonDTO customerByIdPurchaseSubReasonDTO = new CustomerByIdPurchaseSubReasonDTO();
                                customerByIdPurchaseSubReasonDTO.setId(purchaseReason.getId());
                                customerByIdPurchaseSubReasonDTO.setNameEn(purchaseSubReason.getNameEn());
                                customerByIdPurchaseSubReasonDTO.setNameAr(purchaseSubReason.getNameAr());
                                customerByIdPurchaseReasonDTO.setSubReason(customerByIdPurchaseSubReasonDTO);
                            }
                        }
                        customerByIdPurchaseReasonDTOArrayList.add(customerByIdPurchaseReasonDTO);
                        customerByIdPurchaseReasonDTOArrayList.sort(
                                Comparator.comparing(CustomerByIdPurchaseReasonDTO::getCreatedAt).reversed());
                    }
                }
            }
        }
        Boolean newCustomer = null;
        if (!histories.isEmpty()) {
            CustomerProjectHistory latestHistory = histories.stream()
                    .max(Comparator.comparing(CustomerProjectHistory::getCreatedAt))
                    .orElse(null);

            if (latestHistory != null) {
                newCustomer = latestHistory.getNewCustomer();
            }
        }

        List<Integer> projectIds = histories.stream()
                .map(CustomerProjectHistory::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        ProfileResponseDTO dto = new ProfileResponseDTO(
                "",
                existing.getId(),
                existing.getFullName(),
                existing.getMobile(),
                existing.getEmail(),
                existing.getNationality(),
                existing.getAddress(),
                existing.getVerified(),
                existing.getNationalId(),
                existing.getPassportNumber(),
                existing.getCountryCode(),
                existing.getEducation(),
                existing.getOccupation(),
                existing.getGender(),
                existing.getCountry(),
                existing.getLandline(),
                existing.getArea(),
                existing.getCity(),
                existing.getBirthdate(),
                existing.getIsMarried(),
                existing.getNumberOfChildren(),
                formattedPercentage,
                existing.getProfilePictureImageType(),
                existing.getNationalIdImageType());
        dto.setFcmToken(existing.getFcmToken());
        dto.setNewCustomer(newCustomer);
        dto.setProjectIds(projectIds);
        dto.setCustomerPurchaseReasonList(customerByIdPurchaseReasonDTOArrayList);

        if (Integer.parseInt(formattedPercentage) >= 100) {
            dto.setShowPopup(false);
            dto.setPopupContent(null);
        } else {
            configurationService.getActiveConfig("profile_popup").ifPresent(config -> {
                if (configurationService.shouldShowConfig(Long.valueOf(customerId), config.getConfigKey())) {
                    dto.setShowPopup(true);
                    dto.setPopupContent(config.getConditionValue());
                    dto.setPopupTitle(config.getConditionType());
                    configurationService.markConfigShown(Long.valueOf(customerId), config.getConfigKey());
                } else {
                    dto.setShowPopup(false);
                    dto.setPopupContent(null);
                }
            });
            configurationService.getActiveConfig("purchase_reason").ifPresent(config -> {
                dto.setActivePurchaseReason(config.getIsActive());
            });
        }
        return new ReturnObject<>("Profile retrieved successfully", true, dto);
    }

    public ReturnObject<ProfileResponseDTO> getProfileByMobile(Integer mobile, String countryCode) {
        Optional<Customer> customerOpt;
        if (countryCode == null || countryCode.isEmpty()) {
            customerOpt = customerRepository.findByMobile(String.valueOf(mobile));
        } else {
            customerOpt = customerRepository.findByCountryCodeAndMobile(countryCode, String.valueOf(mobile));
        }

        if (customerOpt.isEmpty()) {
            return new ReturnObject<>("Customer not found", false, null);
        }

        Customer existing = customerOpt.get();

        double percentage = calculateProfileCompletionStatic(existing);
        String formattedPercentage = String.format("%.0f", percentage);

        List<CustomerProjectHistory> histories = customerProjectHistoryRepository.findByCustomerId(existing.getId());

        Boolean newCustomer = null;

        if (!histories.isEmpty()) {
            CustomerProjectHistory latestHistory = histories.stream()
                    .max(Comparator.comparing(CustomerProjectHistory::getCreatedAt))
                    .orElse(null);

            if (latestHistory != null) {
                newCustomer = latestHistory.getNewCustomer();
            }
        }

        List<Integer> projectIds = histories.stream()
                .map(CustomerProjectHistory::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        ProfileResponseDTO dto = new ProfileResponseDTO(
                "",
                existing.getId(),
                existing.getFullName(),
                existing.getMobile(),
                existing.getEmail(),
                existing.getNationality(),
                existing.getAddress(),
                existing.getVerified(),
                existing.getNationalId(),
                existing.getPassportNumber(),
                existing.getCountryCode(),
                existing.getEducation(),
                existing.getOccupation(),
                existing.getGender(),
                existing.getCountry(),
                existing.getLandline(),
                existing.getArea(),
                existing.getCity(),
                existing.getBirthdate(),
                existing.getIsMarried(),
                existing.getNumberOfChildren(),
                formattedPercentage,
                existing.getProfilePictureImageType(),
                existing.getNationalIdImageType());
        dto.setFcmToken(existing.getFcmToken());
        dto.setNewCustomer(newCustomer);
        dto.setProjectIds(projectIds);

        return new ReturnObject<>("Profile retrieved successfully", true, dto);
    }

    public List<ProfileResponseDTO> getProfilesByIds(List<Integer> customerIds, String name, String mobile) {
        if (customerIds == null || customerIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Customer> customers = customerRepository.findAllByIdInAndOptionalFilters(customerIds, name, mobile);
        if (customers.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch all customers in one DB call
        Stream<Customer> stream = customers.stream();

        if (name != null && !name.trim().isEmpty()) {
            String lowerName = name.toLowerCase();
            stream = stream
                    .filter(c -> (c.getFullName() != null && c.getFullName().toLowerCase().contains(lowerName)) ||
                            (c.getArabicFullName() != null && c.getArabicFullName().toLowerCase().contains(lowerName)));
        }

        if (mobile != null && !mobile.trim().isEmpty()) {
            stream = stream.filter(c -> c.getMobile() != null && c.getMobile().contains(mobile));
        }

        return stream.map(existing -> {
            ProfileResponseDTO dto = new ProfileResponseDTO(
                    "",
                    existing.getId(),
                    existing.getFullName(),
                    existing.getMobile(),
                    existing.getEmail(),
                    existing.getNationality(),
                    existing.getAddress(),
                    existing.getVerified(),
                    existing.getNationalId(),
                    existing.getPassportNumber(),
                    existing.getCountryCode(),
                    existing.getEducation(),
                    existing.getOccupation(),
                    existing.getGender(),
                    existing.getCountry(),
                    existing.getLandline(),
                    existing.getArea(),
                    existing.getCity(),
                    existing.getBirthdate(),
                    existing.getIsMarried(),
                    existing.getNumberOfChildren(),
                    String.valueOf(calculateProfileCompletionStatic(existing)),
                    existing.getProfilePictureImageType(),
                    existing.getNationalIdImageType());

            dto.setFcmToken(existing.getFcmToken());

            return dto;
        }).collect(Collectors.toList());
    }

    private double calculateProfileCompletionStatic(Customer customer) {
        int totalFields = 13; // number of fields we’re counting
        int filledFields = 0;

        if (isFilled(customer.getFullName()))
            filledFields++;
        // if (isFilled(customer.getArabicFullName())) filledFields++;
        if (isFilled(customer.getMobile()))
            filledFields++;
        if (isFilled(customer.getEmail()))
            filledFields++;
        if (isFilled(customer.getNationality()))
            filledFields++;
        if (isFilled(customer.getAddress()))
            filledFields++;
        if (isFilled(customer.getNationalId()) || isFilled(customer.getPassportNumber()))
            filledFields++;
        if (isFilled(customer.getEducation()))
            filledFields++;
        if (isFilled(customer.getOccupation()))
            filledFields++;
        if (isFilled(customer.getGender()))
            filledFields++;
        if (isFilled(customer.getCountry()))
            filledFields++;
        if (isFilled(customer.getLandline()))
            filledFields++;
        if (isFilled(customer.getArea()))
            filledFields++;
        if (isFilled(customer.getBirthdate()))
            filledFields++;

        return ((double) filledFields / totalFields) * 100;
    }

    public ReturnObject<ProfileResponseDTO> updateProfile(Long customerId, ProfileUpdateRequestDTO updatedProfile) {

        ReturnObject<ProfileResponseDTO> response = new ReturnObject<>();

        Optional<Customer> existingOpt = customerRepository.findById(customerId);
        if (existingOpt.isEmpty()) {
            response.setStatus(false);
            response.setMessage("Customer not found");
            response.setData(null);
            return response;
        }

        // customer type
        if (updatedProfile.getNewCustomer() != null) {

            SubmitCustomerTypeDTO dto = new SubmitCustomerTypeDTO();
            dto.setNewCustomer(updatedProfile.getNewCustomer());
            dto.setProjectIds(updatedProfile.getProjectIds());

            ReturnObject<Void> ctResult = saveCustomerType(customerId, dto);

            if (!ctResult.getStatus()) {
                return new ReturnObject<>(ctResult.getMessage(), false, null);
            }
        }

        Customer existing = existingOpt.get();

        boolean hasPayments = paymentRepository.existsByCustomerId(customerId);

        if (updatedProfile.getEmail() != null) {
            boolean sameEmail = Objects.equals(existing.getEmail(), updatedProfile.getEmail());

            if (!sameEmail && customerRepository.existsByEmail(updatedProfile.getEmail())) {
                response.setStatus(false);
                response.setMessage("Customer email already exists");
                response.setData(null);
                return response;
            }
        }

        CustomerValidator.setNationality(
                updatedProfile.getNationality() != null
                        ? updatedProfile.getNationality()
                        : existing.getNationality());

        String error;
        if (!hasPayments) {
            error = validateField("fullName", updatedProfile.getFullName());
            if (error != null) {
                response.setStatus(false);
                response.setMessage(error);
                response.setData(null);
                return response;
            }

            error = validateField("nationality", updatedProfile.getNationality());
            if (error != null) {
                response.setStatus(false);
                response.setMessage(error);
                response.setData(null);
                return response;
            }

            if (updatedProfile.getGender() == null || updatedProfile.getGender().isBlank()) {
                response.setStatus(false);
                response.setMessage("Gender is required");
                response.setData(null);
                return response;
            }

            error = validateField("address", updatedProfile.getAddress());
            if (error != null) {
                response.setStatus(false);
                response.setMessage(error);
                response.setData(null);
                return response;
            }
            error = validateField("city", updatedProfile.getCity());
            if (error != null) {
                response.setStatus(false);
                response.setMessage(error);
                response.setData(null);
                return response;
            }

        }

        String nationality = updatedProfile.getNationality() != null
                ? updatedProfile.getNationality()
                : existing.getNationality();

        if ("EG".equalsIgnoreCase(nationality)) {

            String nationalId = updatedProfile.getNationalId() != null
                    ? updatedProfile.getNationalId()
                    : existing.getNationalId();

            if ((nationalId == null || nationalId.trim().isEmpty()) &&
                    (existing.getNationalId() == null || existing.getNationalId().trim().isEmpty())) {

                response.setStatus(false);
                response.setMessage("National ID is required for Egyptian customers");
                response.setData(null);
                return response;
            }

        } else {

            String passportNumber = updatedProfile.getPassportNumber() != null
                    ? updatedProfile.getPassportNumber()
                    : existing.getPassportNumber();

            if ((passportNumber == null || passportNumber.trim().isEmpty()) &&
                    (existing.getPassportNumber() == null || existing.getPassportNumber().trim().isEmpty())) {

                response.setStatus(false);
                response.setMessage("Passport number is required for non-Egyptian customers");
                response.setData(null);
                return response;
            }
        }

        try {
            if (validateImageType(updatedProfile.getNationalIdPicture())
                    && updatedProfile.getNationalIdPicture() != null
                    && !updatedProfile.getNationalIdPicture().isEmpty()) {
                existing.setNationalIdImage(updatedProfile.getNationalIdPicture().getBytes());
                existing.setNationalIdImageType(updatedProfile.getNationalIdPicture().getContentType());
            } else {
                System.out.println("Failed To Save National ID Image For Incompatible Validation");
            }
            if (updatedProfile.getProfilePictureDelete() != null
                    && updatedProfile.getProfilePictureDelete().equals(true)) {
                existing.setProfilePicture(null);
                existing.setProfilePictureImageType(null);
            }
            if (validateImageType(updatedProfile.getProfilePicture()) && updatedProfile.getProfilePicture() != null
                    && !updatedProfile.getProfilePicture().isEmpty()) {
                existing.setProfilePicture(updatedProfile.getProfilePicture().getBytes());
                existing.setProfilePictureImageType(updatedProfile.getProfilePicture().getContentType());
            } else {
                System.out.println("Failed To Save Profile Image For Incompatible Validation");
            }
            if (updatedProfile.getNationalIdPictureDelete() != null && updatedProfile.getNationalIdPictureDelete()) {
                existing.setNationalIdImage(null);
                existing.setNationalIdImageType(null);
            }

        } catch (IOException e) {
            System.out.println("Error while uploading images : " + e.getMessage());
            // return new ReturnObject<>(false, "Error reading image files");
        }

        if (hasPayments) {
            existing.setEmail(updatedProfile.getEmail()); // optional updates only
            existing.setCountry(updatedProfile.getCountry());

            existing.setIsMarried(updatedProfile.getIsMarried());
            existing.setNumberOfChildren(updatedProfile.getNumberOfChildren());

            existing.setLandline(updatedProfile.getLandline());
            existing.setArea(updatedProfile.getArea());
            if (updatedProfile.getBirthdate() != null) {
                LocalDate parsed = parseFlexibleDate(updatedProfile.getBirthdate());
                existing.setBirthdate(parsed.toString());
            }
        } else {
            existing.setFullName(updatedProfile.getFullName());
            // existing.setMobile(updatedProfile.getMobile());
            existing.setNationality(updatedProfile.getNationality());
            existing.setAddress(updatedProfile.getAddress());
            existing.setNationalId(updatedProfile.getNationalId());
            existing.setPassportNumber(updatedProfile.getPassportNumber());
            existing.setEmail(updatedProfile.getEmail());

            existing.setIsMarried(updatedProfile.getIsMarried());
            existing.setNumberOfChildren(updatedProfile.getNumberOfChildren());
            // -----
            existing.setEducation(updatedProfile.getEducation());
            existing.setOccupation(updatedProfile.getOccupation());
            existing.setCountry(updatedProfile.getCountry());
            existing.setLandline(updatedProfile.getLandline());
            existing.setArea(updatedProfile.getArea());
            existing.setCity(updatedProfile.getCity());
            if (updatedProfile.getBirthdate() != null) {
                LocalDate parsed = parseFlexibleDate(updatedProfile.getBirthdate());
                existing.setBirthdate(parsed.toString());
            }
            existing.setGender(updatedProfile.getGender());

        }

        existing.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(existing);

        ProfileResponseDTO dto = new ProfileResponseDTO(
                "",
                existing.getId(),
                existing.getFullName(),
                existing.getMobile(),
                existing.getEmail(),
                existing.getNationality(),
                existing.getAddress(),
                existing.getVerified(),
                existing.getNationalId(),
                existing.getPassportNumber(),
                existing.getCountryCode(),
                existing.getEducation(),
                existing.getOccupation(),
                existing.getGender(),
                existing.getCountry(),
                existing.getLandline(),
                existing.getArea(),
                existing.getCity(),
                existing.getBirthdate(),
                existing.getIsMarried(),
                existing.getNumberOfChildren(),
                String.format("%.0f", calculateProfileCompletionStatic(existing)),
                existing.getProfilePictureImageType(),
                existing.getNationalIdImageType());
        List<CustomerProjectHistory> histories = customerProjectHistoryRepository.findByCustomerId(customerId);

        Boolean newCustomer = null;

        List<Integer> projectIds = new ArrayList<>();

        if (!histories.isEmpty()) {

            CustomerProjectHistory latestHistory = histories.stream()
                    .max(Comparator.comparing(
                            CustomerProjectHistory::getCreatedAt,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .orElse(null);

            if (latestHistory != null) {
                newCustomer = latestHistory.getNewCustomer();
            }

            projectIds = histories.stream()
                    .map(CustomerProjectHistory::getProjectId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }

        dto.setNewCustomer(newCustomer);
        dto.setProjectIds(projectIds);

        response.setStatus(true);
        response.setMessage("Profile updated successfully");
        response.setData(dto);

        return response;
    }

    // public ReturnObject<String> resetPassword(String identifier, String
    // countryCode) {
    // String trimmedIdentifier = identifier == null ? null : identifier.trim();
    // String trimmedCountryCode = countryCode == null ? null : countryCode.trim();
    //
    // if (trimmedIdentifier == null || trimmedIdentifier.isBlank()) {
    // return new ReturnObject<>("Identifier (phone or email) is required", false,
    // null);
    // }
    //
    // Optional<Customer> optionalCustomer;
    //
    // if (trimmedIdentifier.contains("@")) {
    // optionalCustomer = customerRepository.findByMobileOrEmail(trimmedIdentifier,
    // trimmedIdentifier);
    // } else {
    // if (trimmedCountryCode == null || trimmedCountryCode.isBlank()) {
    // return new ReturnObject<>("Country code is required when using phone number",
    // false, null);
    // }
    // optionalCustomer =
    // customerRepository.findByCountryCodeAndMobile(trimmedCountryCode,
    // trimmedIdentifier);
    // }
    //
    // if (optionalCustomer.isEmpty()) {
    // return new ReturnObject<>("Customer not found", false, null);
    // }
    //
    // Customer customer = optionalCustomer.get();
    //
    // if (!Boolean.TRUE.equals(customer.getVerified())) {
    // return new ReturnObject<>("Customer not verified", false, null);
    // }
    //
    // String otp = "1111";
    // customer.setResetPasswordOtp(otp);
    // customer.setResetPasswordOtpSentAt(LocalDateTime.now());
    // customerRepository.save(customer);
    //
    // String maskedPhone = maskPhone(customer.getMobile());
    // String maskedEmail = maskEmail(customer.getEmail());
    //
    // String sentTo;
    // if ("+20".equals(customer.getCountryCode())) {
    // System.out.println("Sending password reset OTP " + otp + " to phone: " +
    // maskedPhone);
    // sentTo = "OTP sent to your phone number: " + maskedPhone;
    // } else {
    // System.out.println("Sending password reset OTP " + otp + " to email: " +
    // maskedEmail);
    // sentTo = "OTP sent to your email: " + maskedEmail;
    // }
    //
    // return new ReturnObject<>(sentTo, true, null);
    // }

    public ReturnObject<String> resetPassword(String identifier, String countryCode) {

        String trimmedIdentifier = identifier == null ? null : identifier.trim();
        String trimmedCountryCode = countryCode == null ? null : countryCode.trim();

        if (trimmedIdentifier == null || trimmedIdentifier.isBlank()) {
            return new ReturnObject<>("Identifier (phone or email) is required", false, null);
        }

        Optional<Customer> optionalCustomer;

        if (trimmedIdentifier.contains("@")) {
            optionalCustomer = customerRepository.findByMobileOrEmail(trimmedIdentifier, trimmedIdentifier);
        } else {
            if (trimmedCountryCode == null || trimmedCountryCode.isBlank()) {
                return new ReturnObject<>("Country code is required when using phone number", false, null);
            }
            optionalCustomer = customerRepository.findByCountryCodeAndMobile(trimmedCountryCode, trimmedIdentifier);
        }

        if (optionalCustomer.isEmpty()) {
            return new ReturnObject<>("Customer not found", false, null);
        }

        Customer customer = optionalCustomer.get();

        LocalDateTime lastResetSentTime = customer.getResetPasswordOtpSentAt();
        LocalDateTime currentTime = LocalDateTime.now();

        if (lastResetSentTime != null) {
            long secondsSinceLastSend = ChronoUnit.SECONDS.between(lastResetSentTime, currentTime);
            long cooldownSeconds = 90;

            if (secondsSinceLastSend < cooldownSeconds) {
                long secondsRemaining = cooldownSeconds - secondsSinceLastSend;

                return new ReturnObject<>(
                        "Please wait " + secondsRemaining + " seconds before requesting a new password reset.",
                        false,
                        null);
            }
        }

        String otp = String.valueOf(100000 + new SecureRandom().nextInt(900000));

        customer.setResetPasswordOtp(otp);

        customer.setResetPasswordOtpSentAt(currentTime);
        customerRepository.save(customer);

        String maskedPhone = maskPhone(customer.getMobile());
        String maskedEmail = maskEmail(customer.getEmail());

        String message = "Your password reset OTP is: " + otp + ". This code expires in 3 minutes.";
        String sentTo;

        if ("+20".equals(customer.getCountryCode())) {

            String fullMobile = customer.getCountryCode() + customer.getMobile();

            System.out.println("Sent Phone Number To : " + fullMobile);
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
            boolean sent = smsService.sendSms(fullMobile, message);

            if (!sent) {
                return new ReturnObject<>("Failed to send SMS", false, null);
            }

            sentTo = "OTP sent to your phone number: " + maskedPhone + ". It expires in 3 minutes.";

        } else {
            String htmlMessage = "<div style='font-family: Arial, sans-serif; padding: 20px; background:#f7f7f7;'>" +
                    "<div style='max-width:500px; margin:auto; background:white; padding:20px; " +
                    "border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.1);'>" +
                    "<h2 style='color:#1a73e8;'>Password Reset Verification</h2>" +
                    "<p>Hello,</p>" +
                    "<p>Your password reset OTP is:</p>" +
                    "<div style='font-size:28px; font-weight:bold; color:#1a73e8; margin:20px 0;'>"
                    + otp +
                    "</div>" +
                    "<p>This OTP will expire in <b>3 minutes</b>.</p>" +
                    "<p>If you didn’t request this reset, please ignore this email.</p>" +
                    "<br><hr>" +
                    "<p style='font-size:12px; color:#777;'>© Talaat Moustafa Group</p>" +
                    "</div>" +
                    "</div>";

            System.out.println("Sent Email To : " + customer.getEmail());
            emailService.sendEmail(customer.getEmail(), "Password Reset OTP", htmlMessage);

            sentTo = "OTP sent to your email: " + maskedEmail + ". It expires in 3 minutes.";
        }

        return new ReturnObject<>(sentTo, true, null);
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

    public ReturnObject<String> verifyResetOtp(String identifier, String otp, String countryCode) {
        String trimmedIdentifier = identifier == null ? null : identifier.trim();
        String trimmedOtp = otp == null ? null : otp.trim();
        String trimmedCountryCode = countryCode == null ? null : countryCode.trim();

        if (trimmedIdentifier == null || trimmedIdentifier.isBlank()) {
            return new ReturnObject<>("Identifier (phone or email) is required", false, null);
        }

        Optional<Customer> optionalCustomer;

        if (trimmedIdentifier.contains("@")) {
            optionalCustomer = customerRepository.findByMobileOrEmail(trimmedIdentifier, trimmedIdentifier);
        } else {
            if (trimmedCountryCode == null || trimmedCountryCode.isBlank()) {
                return new ReturnObject<>("Country code is required when using phone number", false, null);
            }
            optionalCustomer = customerRepository.findByCountryCodeAndMobile(trimmedCountryCode, trimmedIdentifier);
        }

        if (optionalCustomer.isEmpty()) {
            return new ReturnObject<>("Customer not found", false, null);
        }

        Customer customer = optionalCustomer.get();

        if (customer.getResetPasswordOtp() == null || customer.getResetPasswordOtpSentAt() == null) {
            return new ReturnObject<>("No OTP found. Please request a new one.", false, null);
        }

        // otp expiry
        LocalDateTime expiryTime = customer.getResetPasswordOtpSentAt().plusMinutes(3);
        if (LocalDateTime.now().isAfter(expiryTime)) {
            return new ReturnObject<>("OTP expired. Please request a new one.", false, null);
        }

        if (!trimmedOtp.equals(customer.getResetPasswordOtp())) {
            return new ReturnObject<>("Invalid OTP.", false, null);
        }

        if (!Boolean.TRUE.equals(customer.getVerified())) {
            customer.setVerified(true);
        }

        customer.setResetPasswordOtp(null);
        customer.setResetPasswordOtpSentAt(null);
        customerRepository.save(customer);

        return new ReturnObject<>("OTP verified successfully.", true, null);
    }

    public ReturnObject<String> confirmPassword(ConfirmPasswordRequestDTO request) {
        String trimmedIdentifier = request.getIdentifier() == null ? null : request.getIdentifier().trim();
        String trimmedCountryCode = request.getCountryCode() == null ? null : request.getCountryCode().trim();

        if (trimmedIdentifier == null || trimmedIdentifier.isBlank()) {
            return new ReturnObject<>("Identifier (phone or email) is required", false, null);
        }

        Optional<Customer> optionalCustomer;

        if (trimmedIdentifier.contains("@")) {
            optionalCustomer = customerRepository.findByMobileOrEmail(trimmedIdentifier, trimmedIdentifier);
        } else {
            if (trimmedCountryCode == null || trimmedCountryCode.isBlank()) {
                return new ReturnObject<>("Country code is required when using phone number", false, null);
            }
            optionalCustomer = customerRepository.findByCountryCodeAndMobile(trimmedCountryCode, trimmedIdentifier);
        }

        if (optionalCustomer.isEmpty()) {
            return new ReturnObject<>("Customer not found", false, null);
        }

        Customer customer = optionalCustomer.get();

        if (!Boolean.TRUE.equals(customer.getVerified())) {
            return new ReturnObject<>("Customer not verified", false, null);
        }

        String passwordValidationError = validateField("password", request.getNewPassword());
        if (passwordValidationError != null) {
            return new ReturnObject<>(passwordValidationError, false, null);
        }

        String repeatPasswordValidationError = validateField("repeatPassword", request.getConfirmPassword());
        if (repeatPasswordValidationError != null) {
            return new ReturnObject<>(repeatPasswordValidationError, false, null);
        }

        String matchValidationError = validatePasswords(
                request.getNewPassword(),
                request.getConfirmPassword());
        if (matchValidationError != null) {
            return new ReturnObject<>(matchValidationError, false, null);
        }

        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);

        return new ReturnObject<>("Password updated successfully.", true, null);
    }

    public ReturnObject<ComplainResponseDTO> addComplain(Customer customer, String description) {
        ReturnObject<ComplainResponseDTO> result = new ReturnObject<>();
        try {
            if (customer == null) {
                result.setStatus(false);
                result.setMessage("Customer not found");
                result.setData(null);
                return result;
            }

            if (description == null || description.trim().isEmpty()) {
                result.setStatus(false);
                result.setMessage("The description cannot be empty");
                result.setData(null);
                return result;
            }

            LocalDateTime fromTime = LocalDateTime.now().minusMinutes(complainLimit.getDurationMinutes());
            long recentComplains = complainRepository.countRecentComplains(customer.getId(), fromTime);

            if (recentComplains >= complainLimit.getLimitCount()) {
                result.setStatus(false);
                result.setMessage(
                        "You can only submit up to " + complainLimit.getLimitCount() + " complaints every 1 Hour.");
                result.setData(null);
                return result;
            }

            Complain complain = new Complain();
            complain.setCustomerId(customer.getId());
            complain.setDescription(description);

            Complain saved = complainRepository.save(complain);

            ComplainResponseDTO response = new ComplainResponseDTO(
                    saved.getId(),
                    saved.getDescription());

            result.setStatus(true);
            result.setMessage("Complaint submitted successfully");
            result.setData(response);

        } catch (Exception e) {
            result.setStatus(false);
            result.setMessage("Failed to send complaint: " + e.getMessage());
            result.setData(null);
        }
        return result;
    }

    public ReturnObject<ReferralResponseDTO> addReferral(Customer customer, AddReferralDTO dto) {
        ReturnObject<ReferralResponseDTO> result = new ReturnObject<>();
        if (customerRepository.existsByCountryCodeAndMobile(
                dto.getCountryCode(),
                dto.getMobile())) {

            return new ReturnObject<>("Mobile number already exists for this country code", false, null);
        }
        Customer lead = new Customer();
        lead.setFullName(dto.getName());
        lead.setMobile(dto.getMobile());
        lead.setCountryCode(dto.getCountryCode());
        lead.setType(CustomerType.LEAD);
        lead.setHaveOffer(false);
        lead.setVerified(false);
        lead.setReferredBy(customer.getId());
        lead = customerRepository.save(lead);

        smsService.sendSms(lead.getCountryCode() + lead.getMobile(),
                "You have been referred by " + customer.getFullName());

        /*
         * Leads lead = new Leads();
         * lead.setCustomerId(customer.getId());
         * lead.setName(dto.getName());
         * lead.setMobile(dto.getMobile());
         * 
         * Leads saved = leadsRepository.save(lead);
         */

        ReferralResponseDTO response = new ReferralResponseDTO(
                lead.getId(),
                lead.getFullName(),
                lead.getMobile(),
                lead.getCreatedAt());

        result.setStatus(true);
        result.setMessage("Referral added successfully");
        result.setData(response);

        return result;
    }

    public ReturnObject<List<PurchaseReasonResponseDTO>> getAllPurchaseReasons() {

        Locale locale = messageUtil.getCurrentLocale();
        boolean isArabic = locale.getLanguage().equals("ar");

        List<PurchaseReason> reasons = purchaseReasonRepository.findAll();
        List<PurchaseSubReason> subReasons = purchaseSubReasonRepository.findAll();

        List<PurchaseReasonResponseDTO> responseList = reasons.stream().map(r -> {
            PurchaseReasonResponseDTO dto = new PurchaseReasonResponseDTO();
            dto.setId(r.getId());
            dto.setName(isArabic ? r.getNameAr() : r.getNameEn());

            List<PurchaseSubReasonResponseDTO> subs = subReasons.stream()
                    .filter(s -> s.getMainReasonId().equals(r.getId()))
                    .map(s -> new PurchaseSubReasonResponseDTO(
                            s.getId(),
                            isArabic ? s.getNameAr() : s.getNameEn()))
                    .toList();

            dto.setSubReasons(subs);

            return dto;
        }).toList();

        return new ReturnObject<>(
                messageUtil.getMessage("purchase.reasons.success"),
                true,
                responseList);
    }

    public ReturnObject<Void> submitPurchaseReason(Long customerId, SubmitPurchaseReasonDTO dto) {
        if (dto.getReasonId() == null) {
            return new ReturnObject<>(
                    messageUtil.getMessage("purchase.reason.notFound"),
                    false,
                    null);
        }
        Optional<PurchaseReason> mainOpt = purchaseReasonRepository.findById(dto.getReasonId());
        if (mainOpt.isEmpty()) {
            return new ReturnObject<>(
                    messageUtil.getMessage("purchase.reason.notFound"),
                    false,
                    null);
        }

        if (dto.getReasonId() != 2 && dto.getSubReasonId() == null) {
            return new ReturnObject<>(
                    messageUtil.getMessage("purchase.subReason.empty"),
                    false,
                    null);
        }
        Optional<PurchaseSubReason> subReasonExistOptional = purchaseSubReasonRepository.findById(dto.getSubReasonId());
        if (subReasonExistOptional.isEmpty()) {
            if (dto.getSubReasonId() != null && !dto.getSubReasonId().toString().isEmpty()) {
                Optional<PurchaseSubReason> subOpt = purchaseSubReasonRepository.findById(dto.getSubReasonId());
                if (subOpt.isEmpty() || !subOpt.get().getMainReasonId().equals(dto.getReasonId())) {
                    return new ReturnObject<>(
                            messageUtil.getMessage("purchase.subReason.invalid"),
                            false,
                            null);
                }
            }
        }
        CustomerInterest saved = new CustomerInterest();
        saved.setCustomerId(customerId);
        saved.setReasonId(dto.getReasonId());
        saved.setSubReasonId(dto.getSubReasonId());
        customerInterestRepository.save(saved);

        return new ReturnObject<>(
                messageUtil.getMessage("purchase.reason.saved"),
                true,
                null);
    }

    public ResponseEntity<?> getProfilePictureImage(CustomUserPrincipal principal) {
        ReturnObject returnObject = new ReturnObject();
        Optional<Customer> customerOptional = customerRepository.findById(principal.getId());
        if (customerOptional.isEmpty()) {
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("Failed to Find Customer");
            return ResponseEntity.badRequest().body(returnObject);
        }
        Customer customer = customerOptional.get();
        if (customer.getProfilePicture() == null) {
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("Failed to Find Profile Picture");
            return ResponseEntity.badRequest().body(returnObject);
        }

        byte[] imageData = customer.getProfilePicture();
        return ResponseEntity
                .ok()
                .header("Content-Type", "image/jpeg")
                .body(imageData);

    }

    public ResponseEntity<?> getNationalIdPicture(CustomUserPrincipal principal) {
        ReturnObject returnObject = new ReturnObject();
        Optional<Customer> customerOptional = customerRepository.findById(principal.getId());
        if (customerOptional.isEmpty()) {
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("Failed to Find Customer");
            return ResponseEntity.badRequest().body(returnObject);
        }
        Customer customer = customerOptional.get();
        if (customer.getNationalIdImage() == null) {
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("Failed to Find National ID Picture");
            return ResponseEntity.badRequest().body(returnObject);
        }

        byte[] imageData = customer.getNationalIdImage();
        return ResponseEntity
                .ok()
                .header("Content-Type", "image/jpeg")
                .body(imageData);

    }

    @Transactional
    public ReturnObject<Void> saveCustomerType(Long customerId, SubmitCustomerTypeDTO dto) {

        Boolean newCustomer = dto.getNewCustomer();
        List<Integer> projectIds = dto.getProjectIds();

        if (Boolean.TRUE.equals(newCustomer) && projectIds != null && !projectIds.isEmpty()) {
            return new ReturnObject<>("You cannot choose projects when 'new customer' is selected", false, null);
        }

        if (Boolean.FALSE.equals(newCustomer) && (projectIds == null || projectIds.isEmpty())) {
            return new ReturnObject<>("Project ids are required for old customers", false, null);
        }

        customerProjectHistoryRepository.deleteByCustomerId(customerId);
        customerProjectHistoryRepository.flush();

        if (Boolean.FALSE.equals(newCustomer)) {

            Set<Integer> existingProjectIds = projectRepository.findAllById(projectIds)
                    .stream()
                    .map(Project::getId)
                    .collect(Collectors.toSet());

            List<Integer> validProjectIds = projectIds.stream()
                    .filter(existingProjectIds::contains)
                    .toList();

            for (Integer id : validProjectIds) {
                CustomerProjectHistory history = new CustomerProjectHistory();
                history.setCustomerId(customerId);
                history.setNewCustomer(false);
                history.setProjectId(id);
                customerProjectHistoryRepository.save(history);
            }

            List<Integer> invalidIds = projectIds.stream()
                    .filter(id -> !existingProjectIds.contains(id))
                    .toList();

            String msg = "Customer type submitted successfully";
            if (!invalidIds.isEmpty())
                msg += ". Invalid project IDs ignored: " + invalidIds;

            return new ReturnObject<>(msg, true, null);

        }

        CustomerProjectHistory history = new CustomerProjectHistory();
        history.setCustomerId(customerId);
        history.setNewCustomer(true);
        customerProjectHistoryRepository.save(history);

        return new ReturnObject<>("Customer type submitted successfully", true, null);
    }

    public ResponseEntity<?> validateFullyRegistered(Long customerId) {

        ReturnObject<Map<String, Object>> returnObject = new ReturnObject<>();
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (customerOptional.isEmpty()) {
            returnObject.setStatus(false);
            returnObject.setMessage("No Customer Found");
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }

        Customer customer = customerOptional.get();

        List<String> missingFields = new ArrayList<>();

        // Helper lambda to reduce repetition
        Consumer<Map.Entry<String, Object>> checkField = entry -> {
            Object value = entry.getValue();

            boolean isMissing = value == null ||
                    (value instanceof String str && str.trim().isEmpty()) ||
                    (value instanceof byte[] arr && arr.length == 0);

            if (isMissing) {
                missingFields.add(entry.getKey());
            }
        };

        // 🟦 Add ALL fields here — you can remove the ones you don't want later
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("fullName", customer.getFullName());
        fields.put("email", customer.getEmail());
        fields.put("countryCode", customer.getCountryCode());
        fields.put("mobile", customer.getMobile());
        fields.put("nationality", customer.getNationality());
        fields.put("address", customer.getAddress());

        boolean hasNationalId = customer.getNationalId() != null && !customer.getNationalId().trim().isEmpty();
        boolean hasPassport = customer.getPassportNumber() != null && !customer.getPassportNumber().trim().isEmpty();

        if (!hasNationalId && !hasPassport) {
            missingFields.add("nationalIdOrPassportNumber");
        }
        fields.put("education", customer.getEducation());
        fields.put("gender", customer.getGender());
        fields.put("country", customer.getCountry());
        fields.put("area", customer.getArea());
        fields.put("birthdate", customer.getBirthdate());
        fields.put("isMarried", customer.getIsMarried());
        if (customer.getIsMarried() != null && customer.getIsMarried()) {
            fields.put("numberOfChildren", customer.getNumberOfChildren());
        }
        // Run validation
        fields.entrySet().forEach(checkField);

        // Response
        Map<String, Object> data = new HashMap<>();
        data.put("customerId", customerId);
        data.put("valid", missingFields.isEmpty());
        data.put("missingFields", missingFields);

        returnObject.setStatus(missingFields.isEmpty());
        returnObject.setMessage(missingFields.isEmpty() ? "Customer fully registered" : "Missing fields found");
        returnObject.setData(data);

        return ResponseEntity.ok(returnObject);
    }

    public ReturnObject<Boolean> updateFcmToken(Long customerId, UpdateFcmTokenDTO dto) {

        Customer customer = customerRepository.findById(customerId).orElse(null);

        if (customer == null) {
            return new ReturnObject<>("Customer not found", false, false);
        }

        customer.setFcmToken(dto.getFcmToken());
        customerRepository.save(customer);

        return new ReturnObject<>("FCM Token updated successfully", true, true);
    }
}