package com.southchurch.my.security;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
public class OpenApiConfig {

    static {
        // Tell Swagger: "Never ask the user for a Jwt object"
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(Jwt.class);
    }
}
