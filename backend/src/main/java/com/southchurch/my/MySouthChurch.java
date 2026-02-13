package com.southchurch.my;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

// 1. DEFINE THE SECURITY TYPE (Bearer Token)
@SecurityScheme(name = "bearerAuth", // Arbitrary name, used to reference below
		type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")

// 2. APPLY IT GLOBALLY (Every endpoint needs this by default)
@OpenAPIDefinition(info = @Info(title = "My South Church API", version = "v1"), security = @SecurityRequirement(name = "bearerAuth"))

@SpringBootApplication
@EnableCaching
public class MySouthChurch {

	public static void main(String[] args) {
		SpringApplication.run(MySouthChurch.class, args);
	}

}
