package com.messaging.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "com.messaging")
@EntityScan(basePackages = "com.messaging")
@EnableJpaRepositories(basePackages = "com.messaging")
@RestController
public class AppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	@GetMapping("/health")
	public String health() {
		return "OK";
	}

}
