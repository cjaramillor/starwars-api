package com.stw.starwapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients 
public class StarwapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StarwapiApplication.class, args);
	}

}
