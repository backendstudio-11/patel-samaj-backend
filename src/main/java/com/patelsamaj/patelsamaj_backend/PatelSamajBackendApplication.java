package com.patelsamaj.patelsamaj_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PatelSamajBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PatelSamajBackendApplication.class, args);
	}
}
