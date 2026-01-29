package com.banking.easbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EasbankApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasbankApplication.class, args);
		System.out.println("Easbank Application Started Successfully");
	}

}