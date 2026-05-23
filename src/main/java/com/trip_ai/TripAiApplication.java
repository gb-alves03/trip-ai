package com.trip_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.trip_ai"})
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.trip_ai.infra.persistence.repository")
@EntityScan(basePackages = "com.trip_ai.infra.persistence.entity")
public class TripAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripAiApplication.class, args);
	}
}
