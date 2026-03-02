package com.resale.loveresalecustomer.shared;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class CustomerValidator {
    private static String countryCode;
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png");
    public static void setCountryCode(String code) {
        countryCode = code;
    }

    private static String nationality;

    public static void setNationality(String nat) {
        nationality = nat;
    }

    public static String validateField(String fieldName, String value) {
        if (fieldName == null) {
            return "Field name is required";
        }

        switch (fieldName) {
            case "fullName":
                if (value == null || value.trim().isEmpty()) {
                    return "Full name is required";
                }

                if ("EG".equalsIgnoreCase(nationality)) {

                    if (!value.matches("^[\u0600-\u06FF ]{3,100}$")) {
                        return "Full name must be in Arabic only for Egyptian nationality";
                    }

                } else {
                    if (!value.matches("^([\u0600-\u06FF ]|[A-Za-z ]){3,100}$")) {
                        return "Full name must be 3–100 characters in Arabic or English letters";
                    }
                }
                break;



            case "mobile":
                if (value == null || value.trim().isEmpty()) {
                    return "Mobile number is required";
                }

                String trimmedMobile = value.trim().replaceAll("\\s+", "");
                String trimmedCode = (countryCode == null) ? "" : countryCode.trim().replaceAll("\\s+", "").replace("+", "");

                if (trimmedCode.equals("20")) {
                    if (!trimmedMobile.matches("^(10|11|12|15)\\d{8}$")) {
                        return "Invalid Egyptian mobile number format";
                    }

                } else if (!trimmedMobile.matches("^\\d{8,15}$")) {
                    return "Invalid mobile number format";
                }

                break;

            case "email":
                if (value == null || value.trim().isEmpty()) {
                    return "Email is required";
                }
                if (!value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                    return "Invalid email format";
                }
                break;

            case "nationality":
                if (value == null || value.trim().isEmpty()) {
                    return "Nationality is required";
                }
                break;

            case "password":
                if (value == null || value.length() < 8) {
                    return "Password must be at least 8 characters long";
                }
                if (!value.matches(".*[A-Z].*")) {
                    return "Password must contain at least one uppercase letter";
                }
                if (!value.matches(".*[a-z].*")) {
                    return "Password must contain at least one lowercase letter";
                }
                if (!value.matches(".*\\d.*")) {
                    return "Password must contain at least one number";
                }
                if (!value.matches(".*[!@#$%^&*()].*")) {
                    return "Password must contain at least one special character (!@#$%^&*())";
                }
                break;

            case "repeatPassword":
                if (value == null || value.trim().isEmpty()) {
                    return "Repeat password is required";
                }
                break;

            case "address":
                if (value != null && !value.trim().isEmpty()) {
                    if (value.length() < 5 || value.length() > 200) {
                        return "Address must be between 5 and 200 characters if provided";
                    }
                }
                break;

            case "city":
                if ("EG".equalsIgnoreCase(nationality)) {
                    if (value == null || value.trim().isEmpty()) {
                        return "City is required for Egyptian nationality";
                    }
                }
                break;

            case "nationalId":
        if (value != null && !value.trim().isEmpty()) {
            if (!value.matches("\\d{14}")) {
                return "National ID must be 14 digits if provided";
            }
        }
            break;

            case "passportNumber":
                if (value != null && !value.trim().isEmpty()) {
                    if (!value.matches("^[A-Za-z0-9]{6,30}$")) {
                        return "Passport number must be alphanumeric and between 6 and 30 characters";
                    }
                }

            break;
            default:
                return "Unknown field: " + fieldName;
        }

        return null;
    }

    public static String validatePasswords(String password, String repeatPassword) {
        if (password == null || repeatPassword == null) {
            return "Password and repeat password are required";
        }
        if (!password.equals(repeatPassword)) {
            return "Passwords do not match";
        }
        return null;
    }

    public static Boolean validateImageType(MultipartFile file) {
        if(file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            return ALLOWED_TYPES.contains(contentType);
        }else{
            return false;
        }
    }

    public static boolean isFilled(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
