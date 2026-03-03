package com.resale.loveresalecustomer.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {

    public static <T> ResponseEntity<ReturnObject<T>> success(String message, HttpStatus status, T data) {
        ReturnObject<T> returnObject = new ReturnObject<>(message, true, data);
        return ResponseEntity.status(status).body(returnObject);
    }

    public static <T> ResponseEntity<ReturnObject<T>> error(String message, HttpStatus status) {
        ReturnObject<T> returnObject = new ReturnObject<>(message, false, null);
        return ResponseEntity.status(status).body(returnObject);
    }
}