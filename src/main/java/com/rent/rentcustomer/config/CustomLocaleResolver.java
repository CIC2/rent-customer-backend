package com.resale.loveresalecustomer.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class CustomLocaleResolver extends AcceptHeaderLocaleResolver {

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String headerLang = request.getHeader("locale");

        if (headerLang == null || headerLang.isEmpty()) {
            return Locale.ENGLISH;
        }

        List<Locale.LanguageRange> list = Locale.LanguageRange.parse(headerLang);
        Locale locale = Locale.lookup(list, List.of(Locale.ENGLISH, new Locale("ar")));

        return locale != null ? locale : Locale.ENGLISH;
    }
}
