package com.divelink.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DiveLinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiveLinkApplication.class, args);
	}

}
