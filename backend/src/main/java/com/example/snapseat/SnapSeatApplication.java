package com.example.snapseat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SnapSeatApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnapSeatApplication.class, args);
	}

}
