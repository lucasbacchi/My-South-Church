package com.southchurch.my;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.southchurch.my.config.TestGoogleConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestGoogleConfig.class)
class MyApplicationTests {

	@Test
	void contextLoads() {
	}

}
