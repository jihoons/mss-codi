package com.mss.codi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Map;

@EnableJpaAuditing
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(Application.class);
		application.setDefaultProperties(Map.of("spring.profiles.default", "dev"));
		application.run(args);
	}
}
