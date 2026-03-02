package com.resale.loveresalecustomer.components.profile_management.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ProfileUpdateRequestDTO {
    private String fullName;
    private String email;
    private String countryCode;
    private String mobile;
    private String nationality;
    private String address;
    private String city;
    private String nationalId;
    private String occupation;
    private String education;
    private String gender;
    private String country;
    private String area;
    private String birthdate;
    private String street;
    private String building;
    private String floor;
    private String apartment;
    private String passportNumber;
    private String landline;

    private Boolean isMarried;
    private Integer numberOfChildren;

    private MultipartFile nationalIdPicture;
    private MultipartFile profilePicture;

    private Boolean profilePictureDelete;
    private Boolean nationalIdPictureDelete;

    private Boolean newCustomer;
    private List<Integer> projectIds;

}
