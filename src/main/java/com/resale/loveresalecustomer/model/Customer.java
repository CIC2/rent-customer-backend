package com.resale.loveresalecustomer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "arabic_full_name", length = 100)
    private String arabicFullName;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "country_code", length = 50)
    private String countryCode;

    @Column(name = "mobile", length = 20)
    private String mobile;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "have_offer", nullable = false)
    private Boolean haveOffer;

    @Column(name = "verified", nullable = false)
    private Boolean verified;

    @Column(name = "otp", length = 10)
    private String otp;

    @Column(name = "otp_sent_at")
    private LocalDateTime otpSentAt;

    @Column(name = "reset_password_otp", length = 10)
    private String resetPasswordOtp;

    @Column(name = "reset_password_otp_sent_at")
    private LocalDateTime resetPasswordOtpSentAt;

    @Column(name = "national_id", length = 100)
    private String nationalId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked = false;

    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "passport_number", length = 50)
    private String passportNumber;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "education", length = 100)
    private String education;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "area", length = 100)
    private String area;
    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "birthdate", length = 100)
    private String birthdate;

    @Column(name = "landline", length = 50)
    private String landline;

    @Column(name = "is_married")
    private Boolean isMarried;

    @Column(name = "number_of_children")
    private Integer numberOfChildren;

    @Lob
    @Column(name = "national_id_image", columnDefinition = "LONGBLOB")
    private byte[] nationalIdImage;

    // 🆕 Profile Picture
    @Lob
    @Column(name = "profile_picture", columnDefinition = "LONGBLOB")
    private byte[] profilePicture;

    @Column(name = "national_id_image_type")
    private String nationalIdImageType;

    @Column(name = "profile_picture_image_type")
    private String profilePictureImageType;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private CustomerType type;

    @Column(name = "referred_by")
    private Long referredBy;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
