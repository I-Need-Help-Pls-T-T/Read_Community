package com.univer.bookcom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class BookcomApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookcomApplication.class, args);
	}

}
