package com.resale.loveresalecustomer.components.profile_management;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.resale.loveresalecustomer.components.profile_management.dto.*;
import com.resale.loveresalecustomer.model.Customer;
import com.resale.loveresalecustomer.security.CustomUserPrincipal;
import com.resale.loveresalecustomer.utils.ReturnObject;

import java.util.List;

@RestController
@RequestMapping("/profile")
@ControllerAdvice
public class ProfileController {

    @Autowired
    ProfileService profileService;

    @Autowired
    private final Environment environment;

    public ProfileController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("")
    public ResponseEntity<ReturnObject<ProfileResponseDTO>> getProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            HttpServletRequest request) {
        String token = "";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("CUSTOMER_AUTH_TOKEN".equals(cookie.getName())) {
                    System.out.println("JWT: " + cookie.getValue());
                    token = cookie.getValue();
                }
            }
        }
        ReturnObject<ProfileResponseDTO> result = profileService.getMyProfile(token,principal.getId());

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        return ResponseEntity.ok(result);
    }


    @GetMapping("/id")
    public ResponseEntity<ReturnObject<ProfileResponseDTO>> getProfileById(
            @RequestParam("customerId") Long customerId,
            @RequestHeader(value = "X-Internal-Auth", required = false) String internalToken) {

        // ✅ Security check
        String expectedToken = environment.getProperty("internal.auth.token");
        if (internalToken == null || !internalToken.equals(expectedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ReturnObject<>( "Unauthorized internal request",false, null));
        }

        ReturnObject<ProfileResponseDTO> result = profileService.getProfile("",customerId);

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        return ResponseEntity.ok(result);
    }


    @GetMapping("/mobileAndCountryCode")
    public ResponseEntity<ReturnObject<ProfileResponseDTO>> getProfileByMobile(
            @RequestParam("mobile") Integer mobile,
            @RequestParam(value = "countryCode", required = false) String countryCode,
            @RequestHeader(value = "X-Internal-Auth", required = false) String internalToken) {

        // 🔐 Internal token check
        String expectedToken = environment.getProperty("internal.auth.token");
        if (internalToken == null || !internalToken.equals(expectedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ReturnObject<>("Unauthorized internal request", false, null));
        }

        // 🔎 Call your service using mobile + countryCode
        ReturnObject<ProfileResponseDTO> result = profileService.getProfileByMobile(mobile, countryCode);

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/listByIds")
    public ResponseEntity<ReturnObject<List<ProfileResponseDTO>>> getProfilesByIds(
            @RequestParam("customerIds") List<Integer> customerIds,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestHeader(value = "X-Internal-Auth", required = false) String internalToken) {

        String expectedToken = environment.getProperty("internal.auth.token");
        if (internalToken == null || !internalToken.equals(expectedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ReturnObject<>("Unauthorized internal request", false, null));
        }

        try {
  /*          if (customerIds == null || customerIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ReturnObject<>("Customer IDs cannot be empty", false, null));
            }*/

            List<ProfileResponseDTO> profiles = profileService.getProfilesByIds(customerIds, name, mobile);

/*
            if (profiles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ReturnObject<>("No customers found for given IDs", false, null));
            }
*/

            return ResponseEntity.ok(
                    new ReturnObject<>("Customers retrieved successfully", true, profiles)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReturnObject<>("Error retrieving customers: " + e.getMessage(), false, null));
        }
    }


    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReturnObject<ProfileResponseDTO>> updateProfile2(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @ModelAttribute  ProfileUpdateRequestDTO  updatedProfile
    ) {
        ReturnObject<ProfileResponseDTO> result = profileService.updateProfile(principal.getId(), updatedProfile);

        if (!result.getStatus()) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("profilePicture")
//    @LogActivity(ActionType.GET_PP)
    public ResponseEntity<?> getProfilePicture(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return profileService.getProfilePictureImage(principal);
    }

    @GetMapping("validateFullyRegistered")
    public ResponseEntity<?> validateFullyRegistered(@RequestParam("customerId") Long customerId) {
        return profileService.validateFullyRegistered(customerId);
    }

    @GetMapping("nationalIdPicture")
    public ResponseEntity<?> getNationalIdPicture(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return profileService.getNationalIdPicture(principal);
    }


    @PostMapping("/resetPassword")
    public ResponseEntity<ReturnObject<String>> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        ReturnObject<String> result = profileService.resetPassword(request.getIdentifier(), request.getCountryCode());

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        return ResponseEntity.ok(result);
    }


    @PostMapping("/verifyResetOtp")
    public ResponseEntity<ReturnObject<String>> verifyResetOtp(
            @RequestBody VerifyOtpResetPasswordDTO request) {

        ReturnObject<String> result = profileService.verifyResetOtp(
                request.getIdentifier(),
                request.getOtp(),
                request.getCountryCode()
        );

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        return ResponseEntity.ok(result);
    }


    @PostMapping("/confirmPassword")
    public ResponseEntity<ReturnObject<String>> confirmPassword(@RequestBody ConfirmPasswordRequestDTO request) {
        ReturnObject<String> result = profileService.confirmPassword(request);

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        return ResponseEntity.ok(result);
    }


    @PostMapping("/complain")
    public ResponseEntity<ReturnObject<ComplainResponseDTO>> addComplain(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody AddComplainDTO dto) {

        Customer customer = principal.getCustomer();
        ReturnObject<ComplainResponseDTO> result = profileService.addComplain(customer, dto.getDescription());

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @PostMapping("/referral")
    public ResponseEntity<ReturnObject<ReferralResponseDTO>> addReferral(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AddReferralDTO dto) {

        Customer customer = principal.getCustomer();
        ReturnObject<ReferralResponseDTO> result = profileService.addReferral(customer, dto);

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @GetMapping("/purchaseReasons")
    public ResponseEntity<ReturnObject<List<PurchaseReasonResponseDTO>>> getPurchaseReasons() {
        ReturnObject<List<PurchaseReasonResponseDTO>> response = profileService.getAllPurchaseReasons();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/purchaseReason")
    public ResponseEntity<ReturnObject<Void>> submitPurchaseReason(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody SubmitPurchaseReasonDTO dto) {

        ReturnObject<Void> response = profileService.submitPurchaseReason(principal.getId(), dto);

        return new ResponseEntity<>(
                response,
                response.getStatus() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        );
    }


    @PostMapping("/submitCustomerType")
    public ResponseEntity<ReturnObject<Void>> submitCustomerType(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody SubmitCustomerTypeDTO dto
    ) {
        ReturnObject<Void> response = profileService.saveCustomerType(principal.getId(), dto);

        HttpStatus httpStatus = response.getStatus() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, httpStatus);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnObject<ProfileResponseDTO>> getProfileById(@PathVariable Long id) {
        ReturnObject<ProfileResponseDTO> result = profileService.getProfile("",id);

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        return ResponseEntity.ok(result);
    }


    @PostMapping("/logout")
    public ResponseEntity<ReturnObject<Void>> logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("CUSTOMER_AUTH_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);


        return ResponseEntity.ok(new ReturnObject<>("Logout successful", true, null));
    }


    @PutMapping("/updateFcmToken")
    public ResponseEntity<?> updateFcmToken(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody UpdateFcmTokenDTO dto
    ) {
        Long customerId = principal.getId();

        ReturnObject<Boolean> result = profileService.updateFcmToken(customerId, dto);

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        return ResponseEntity.ok(result);
    }
}
