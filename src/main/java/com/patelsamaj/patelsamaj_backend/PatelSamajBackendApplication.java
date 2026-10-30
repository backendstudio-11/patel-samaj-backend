package com.patelsamaj.patelsamaj_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PatelSamajBackendApplication {

	public static void main(String[] args) {
		Dotenv.configure()
				.ignoreIfMissing()
				.load()
				.entries()
				.forEach(e -> System.setProperty(e.getKey(), e.getValue()));

		SpringApplication.run(PatelSamajBackendApplication.class, args);
	}
}
