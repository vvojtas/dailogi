package com.github.vvojtas.dailogi_server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

@SpringBootTest
@Profile("test")
class DailogiServerApplicationTests {

	@Test
	void contextLoads() {
		assertTrue(true);
	}

}
