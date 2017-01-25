package it.bz.idm.carsharing;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {
	
	
	 @Scheduled(fixedRate = 5000)
	public void staticTask() {
		System.err.println("Static Task");
	}
	
	 @Scheduled(cron="0 15 10 * * *")
	public void realTimeTask() {
		System.err.println("Real Time Task");
	}
}
