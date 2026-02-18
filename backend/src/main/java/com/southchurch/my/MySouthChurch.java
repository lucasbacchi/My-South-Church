package com.southchurch.my;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

// Define the security scheme for Swagger (Bearer Token)
@SecurityScheme(name = "bearerAuth", // Arbitrary name, used to reference below
		type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")

// Default to requiring authentication for all endpoints in Swagger UI
@OpenAPIDefinition(info = @Info(title = "My South Church API", version = "v1"), security = @SecurityRequirement(name = "bearerAuth"))

@SpringBootApplication
@EnableCaching
@EnableAsync(proxyTargetClass = true)
public class MySouthChurch {

	public static void main(String[] args) {
		SpringApplication.run(MySouthChurch.class, args);
	}

}
