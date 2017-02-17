package it.bz.idm.carsharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan
@SpringBootApplication
@EnableScheduling
public class BootTestApplication {
	public static void main(String[] args) {
		SpringApplication.run(BootTestApplication.class, args);
	}
}