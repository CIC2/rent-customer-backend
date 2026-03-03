package com.resale.loveresalecustomer.security;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.util.Map;

import org.springframework.web.util.HtmlUtils;

@ControllerAdvice
public class XSSRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // apply globally
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage,
                                MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {

        sanitizeObject(body);
        return body;
    }

    private void sanitizeObject(Object body) {
        if (body == null) return;

        // Skip primitive wrappers, String, Number, Boolean, List, Map, etc.
        if (body instanceof String || body instanceof Number || body instanceof Boolean || body instanceof Iterable || body instanceof Map) {
            return;
        }

        try {
            for (var field : body.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(body);
                if (value instanceof String strValue) {
                    field.set(body, HtmlUtils.htmlEscape(strValue));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
