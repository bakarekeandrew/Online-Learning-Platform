package com.example.signup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Controller
public class LanguageController {

    private final LocaleResolver localeResolver;
    private final List<String> supportedLanguages = Arrays.asList("en", "fr");

    public LanguageController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @GetMapping("/changeLanguage")
    public String changeLanguage(@RequestParam("lang") String lang,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        // Validate the language parameter
        if (!supportedLanguages.contains(lang.toLowerCase())) {
            lang = "en";
        }

        // Set the new locale
        Locale locale = new Locale(lang);
        localeResolver.setLocale(request, response, locale);

        // Get referer URL
        String referer = request.getHeader("Referer");
        if (referer != null) {
            // Remove any existing lang parameter from referer
            referer = referer.replaceAll("[?&]lang=[^&]*", "");
            // Ensure proper URL structure when adding lang parameter
            return "redirect:" + referer + (referer.contains("?") ? "&" : "?") + "lang=" + lang;
        }

        // Fallback to home page if no referer
        return "redirect:/?lang=" + lang;
    }
}