package it.bz.idm.carsharing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private HashMap<String, List<String>> vehicleIdsByStationIds;

	// private HashMap<String, List<Integer>> vehicleIdsByStationNames;

	// library missing
	// IXMLRPCPusher xmlrpcPusher;

	@Autowired
	private CarsharingConnector carsharingConnector;

	public Scheduler() {
		// vehicleIdsByStationNames = new HashMap<>();
	}

	/**
	 * for getting the static data like vehicle and stationlist from the
	 * carsharingAPI and push them to te integreen-platform
	 */
	@Scheduled(cron = "0 0 0 * * *") // every day at midnight
	// @Scheduled(fixedRate = 18000) // 3 minutes interval FOR TESTING
	public void staticTask() {

		try {
			vehicleIdsByStationIds = carsharingConnector.connectForStaticData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * for getting real time data like the vehicle and stationlist from the
	 * carsharingAPI and push them to te integreen-platform
	 */
	// @Scheduled(fixedRate = 600000) // 10 minutes interval
	// @Scheduled(fixedRate = 12000) // 2 minutes interval FOR TESTING
	@Scheduled(cron = "0 0/10 * * * ?") // every 10 minutes, but at 6.10 PM for
										// example
	public void realTimeTask() {
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
	}
}