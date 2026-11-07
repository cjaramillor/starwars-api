package com.stw.starwapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.stw")
@EnableFeignClients(basePackages = "com.stw.client")
@EntityScan(basePackages = "com.stw.entities")
@EnableJpaRepositories(basePackages = "com.stw.repository")
public class StarwapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StarwapiApplication.class, args);
	}

}
