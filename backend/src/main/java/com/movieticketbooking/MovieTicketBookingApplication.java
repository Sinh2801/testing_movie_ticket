package com.movieticketbooking;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class MovieTicketBookingApplication {

	public static void main(String[] args) {
		try {
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		} catch (Exception e) {
			// Ignore if .env is missing in production environments
		}
		SpringApplication.run(MovieTicketBookingApplication.class, args);
	}

}
