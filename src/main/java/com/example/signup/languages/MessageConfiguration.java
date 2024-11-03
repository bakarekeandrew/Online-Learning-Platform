package com.example.signup.languages;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;


@Configuration
public class MessageConfiguration {

    public MessageSource messageSource(){
        ResourceBundleMessageSource messageSource =new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return  messageSource;
    }
}
