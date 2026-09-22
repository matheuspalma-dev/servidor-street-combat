package com.street.combat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StreetCombatJogoApplication {

	public static void main(String[] args) {
		SpringApplication.run(StreetCombatJogoApplication.class, args);
	}

}
