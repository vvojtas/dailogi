package com.github.vvojtas.dailogi_server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DailogiServerApplicationTests {

	@Test
	void contextLoads() {
		assertTrue(true);
	}

}
