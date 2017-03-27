package it.bz.idm.carsharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarSharingDataCollectorApplication {
	public static void main(String[] args) {
		SpringApplication.run(CarSharingDataCollectorApplication.class, args);
	}
}