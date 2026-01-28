package com.southchurch.my;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class MySouthChurch {

	public static void main(String[] args) {
		SpringApplication.run(MySouthChurch.class, args);
	}

}
