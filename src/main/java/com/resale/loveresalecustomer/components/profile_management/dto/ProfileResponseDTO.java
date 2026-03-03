package com.resale.loveresalecustomer.components.profile_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDTO {
    private String token;
    private Long customerId;
    private String fullName;
    private String mobile;
    private String email;
    private String nationality;
    private String address;
    private Boolean isVerified;
    private String nationalId;
    private String passportNumber;
    private String countryCode;
    //
    private String education;
    private String occupation;
    private String gender;

    private String country;
    private String landline;
    private String area;
    private String city;
    private String birthdate;
    private Boolean isMarried;
    private Integer numberOfChildren;
    private String profileCompletion;
    private String profilePictureImageType;
    private String nationalIdImageType;
    private String fcmToken;
    private Boolean newCustomer;
    private List<Integer> projectIds;
    private Boolean showPopup;
    private String popupContent;
    private String popupTitle;
    private Boolean hasAppointmentToRate;
    private Boolean hasActiveCall;
    private Integer appointmentId;
    private Boolean activePurchaseReason;
    private List<CustomerByIdPurchaseReasonDTO> customerPurchaseReasonList;

    public ProfileResponseDTO(Long customerId, String fullName, String mobile, String email,
            String nationality, String address, Boolean isVerified, String nationalId,
            String passportNumber, String countryCode, String education, String occupation,
            String gender, String country, String landline, String area, String city, String birthdate,
            Boolean isMarried, Integer numberOfChildren,
            String profilePictureImageType, String nationalIdImageType) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.mobile = mobile;
        this.email = email;
        this.nationality = nationality;
        this.address = address;
        this.isVerified = isVerified;
        this.nationalId = nationalId;
        this.passportNumber = passportNumber;
        this.countryCode = countryCode;
        this.education = education;
        this.occupation = occupation;
        this.gender = gender;
        this.country = country;
        this.landline = landline;
        this.area = area;
        this.city = city;
        this.birthdate = birthdate;
        this.isMarried = isMarried;
        this.numberOfChildren = numberOfChildren;
        this.profilePictureImageType = profilePictureImageType;
        this.nationalIdImageType = nationalIdImageType;
    }

    public ProfileResponseDTO(
            String token,
            Long customerId,
            String fullName,
            String mobile,
            String email,
            String nationality,
            String address,
            Boolean isVerified,
            String nationalId,
            String passportNumber,
            String countryCode,
            String education,
            String occupation,
            String gender,
            String country,
            String landline,
            String area,
            String city,
            String birthdate,
            Boolean isMarried,
            Integer numberOfChildren,
            String profileCompletionPercentage,
            String profilePictureImageType,
            String nationalIdImageType) {
        this.token = token;
        this.customerId = customerId;
        this.fullName = fullName;
        this.mobile = mobile;
        this.email = email;
        this.nationality = nationality;
        this.address = address;
        this.isVerified = isVerified;
        this.nationalId = nationalId;
        this.passportNumber = passportNumber;
        this.countryCode = countryCode;
        this.education = education;
        this.occupation = occupation;
        this.gender = gender;
        this.country = country;
        this.landline = landline;
        this.area = area;
        this.city = city;
        this.birthdate = birthdate;
        this.isMarried = isMarried;
        this.numberOfChildren = numberOfChildren;
        this.profileCompletion = profileCompletionPercentage;
        this.profilePictureImageType = profilePictureImageType;
        this.nationalIdImageType = nationalIdImageType;
    }
}