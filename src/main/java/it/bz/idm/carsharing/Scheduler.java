package it.bz.idm.carsharing;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private HashMap<Integer, Integer[]> vehicleIdsByStationIds;

	// library missing
	// IXMLRPCPusher xmlrpcPusher;

	private CarsharingConnector carsharingConnector = null;

	public Scheduler() {
		carsharingConnector = new CarsharingConnector();
	}

	/**
	 * for getting the static data like vehicle and stationlist from the
	 * carsharingAPI and push them to te integreen-platform
	 */
	@Scheduled(cron = "0 0 0 * * *") // every day at midnight
	public void staticTask() {

		logger.info("Static Task started");
		try {
			vehicleIdsByStationIds = carsharingConnector.connectForStaticData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Static Task finished");
	}

	/**
	 * for getting real time data like the vehicle and stationlist from the
	 * carsharingAPI and push them to te integreen-platform
	 */
	@Scheduled(fixedRate = 600000) // 10 minutes interval
	public void realTimeTask() {
		logger.info("Real Time Task");
		try {
			if (vehicleIdsByStationIds != null) {
				carsharingConnector.connectForRealTimeData(vehicleIdsByStationIds);
			} else {
				logger.info("Get Static Data for the first Time");
				vehicleIdsByStationIds = carsharingConnector.connectForStaticData();
				logger.info("Get Static Data finished for the first Time");
				logger.info("Real Time Task for the first Time");
				carsharingConnector.connectForRealTimeData(vehicleIdsByStationIds);
				logger.info("Real Time Task finished for the first Time");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Real Time Task finished");
	}
}