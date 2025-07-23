package com.apelisser.rinha2025;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;

@EnableAsync
@SpringBootApplication
public class RinhaBackend2025Application {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(UTC));
		SpringApplication.run(RinhaBackend2025Application.class, args);
	}

}
