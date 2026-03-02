package com.resale.loveresalecustomer.components.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resale.loveresalecustomer.components.auth.dto.login.LoginRequestDTO;
import com.resale.loveresalecustomer.components.auth.dto.login.LoginResponseDTO;
import com.resale.loveresalecustomer.components.auth.dto.otp.SendOtpRequestDTO;
import com.resale.loveresalecustomer.components.auth.dto.otp.VerifyOtpRequestDTO;
import com.resale.loveresalecustomer.components.auth.dto.register.RegistrationRequestDTO;
import com.resale.loveresalecustomer.components.auth.dto.register.RegistrationResponseDTO;
import com.resale.loveresalecustomer.components.profile_management.dto.ProfileResponseDTO;
import com.resale.loveresalecustomer.utils.ReturnObject;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ReturnObject<RegistrationResponseDTO>> register(@RequestBody RegistrationRequestDTO dto) {
        ReturnObject<RegistrationResponseDTO> result = authService.registerCustomer(dto);

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @PostMapping("/sendOtp")
    public ResponseEntity<ReturnObject<String>> sendOtp(@RequestBody SendOtpRequestDTO request) {
        String countryCode = request.getCountryCode();
        String mobile = request.getMobile();

        ReturnObject<String> result = authService.sendOtp(countryCode, mobile);
        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.ok(result);
    }


    @PostMapping("/verifyOtp")
    public ResponseEntity<ReturnObject<ProfileResponseDTO>> verifyOtp(@RequestBody VerifyOtpRequestDTO request) {
        ReturnObject<ProfileResponseDTO> result = authService.verifyOtp(request);
        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.ok(result);
    }


    @PostMapping("/login")
    public ResponseEntity<ReturnObject<LoginResponseDTO>> login(
            @RequestBody LoginRequestDTO request,
            HttpServletResponse response) {

        ReturnObject<LoginResponseDTO> result = authService.login(request);

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        String token = result.getData().getToken();
        Cookie cookie = new Cookie("CUSTOMER_AUTH_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);

        return ResponseEntity.ok(result);
    }
}