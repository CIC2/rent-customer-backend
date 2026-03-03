package com.resale.loveresalecustomer.components.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.resale.loveresalecustomer.config.SmsConfig;
import com.resale.loveresalecustomer.utils.SmsUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsConfig smsConfig;
    private final RestTemplate restTemplate;
    public boolean sendSms(String mobile, String message) {

        try {
            String xmlBody = buildSmsRequest(mobile, message);

            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);

            HttpEntity<String> request = new HttpEntity<>(xmlBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    smsConfig.getServerAddress(),
                    request,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private String buildSmsRequest(String mobile, String message) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("AccountId", smsConfig.getAccountid());
        fields.put("Password", smsConfig.getApiPassword());
        fields.put("SenderName", smsConfig.getSenderName());
        fields.put("ReceiverMSISDN", mobile);
        fields.put("SMSText", message);

        String secureHash = SmsUtils.generateSecureHash(fields, smsConfig.getSecretKey());

        return SmsUtils.buildSmsXml(
                smsConfig.getAccountid(),
                smsConfig.getApiPassword(),
                smsConfig.getSenderName(),
                mobile,
                message,
                secureHash
        );
    }
}
