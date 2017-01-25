package it.bz.idm.carsharing;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Scheduled(fixedRate = 600000) // 10 minutes interval
	public void staticTask() {
		logger.info("Static Task");
	}

	@Scheduled(cron = "0 0 0 * * *") // every day at midnight
	public void realTimeTask() {
		logger.info("Real Time Task");
	}
}
