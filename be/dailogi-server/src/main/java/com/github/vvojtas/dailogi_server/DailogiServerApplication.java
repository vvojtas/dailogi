package com.github.vvojtas.dailogi_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@EnableAutoConfiguration
@ConfigurationPropertiesScan("com.github.vvojtas.dailogi_server.properties")
public class DailogiServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DailogiServerApplication.class, args);
	}

}
