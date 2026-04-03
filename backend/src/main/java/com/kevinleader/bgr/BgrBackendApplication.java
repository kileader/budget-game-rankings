package com.kevinleader.bgr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BgrBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BgrBackendApplication.class, args);
	}

}
