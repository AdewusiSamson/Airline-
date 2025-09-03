package com.example.Airline_Project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AirlineProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirlineProjectApplication.class, args);
	}

}
