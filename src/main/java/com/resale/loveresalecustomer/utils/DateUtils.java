package com.resale.loveresalecustomer.utils;

import org.apache.commons.text.StringEscapeUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {
    public static LocalDate parseFlexibleDate(String input) {

        // 🔥 Fix double HTML escaping
        input = StringEscapeUtils.unescapeHtml4(input);
        input = StringEscapeUtils.unescapeHtml4(input);
        input = input.trim();

        // Remove the timezone name in parentheses
        input = input.replaceAll("\\s*\\(.*?\\)", "").trim();

        // Parse JS date
        try {
            DateTimeFormatter jsFormatter = DateTimeFormatter.ofPattern(
                    "EEE MMM dd yyyy HH:mm:ss 'GMT'Z",
                    Locale.ENGLISH
            );
            return ZonedDateTime.parse(input, jsFormatter).toLocalDate();
        } catch (Exception ignored) {}

        // Fallback formats
        String[] patterns = {
                "dd-MM-yyyy",
                "dd/MM/yyyy",
                "yyyy-MM-dd",
                "yyyy/MM/dd",
                "dd-MM-yy",
                "dd/MM/yy",
                "MM-dd-yyyy",
                "MM/dd/yyyy"
        };

        for (String pattern : patterns) {
            try {
                return LocalDate.parse(input, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {}
        }

        throw new IllegalArgumentException("Unsupported date format: " + input);
    }
}
