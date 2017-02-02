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
	private HashMap<String, String[]> vehicleIdsByStationIds;

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
		// vehicleIdsByStationIds =
		// carsharingConnector.connectForStaticData(cityUIDs, apiClient);
		System.err.println("REAL TIME");
		logger.info("Static Task finished");
	}

	/**
	 * for getting real time data like the vehicle and stationlist from the
	 * carsharingAPI and push them to te integreen-platform
	 */
	@Scheduled(fixedRate = 600000) // 10 minutes interval
	public void realTimeTask() {
		try {
			logger.info("Real Time Task");
			if (vehicleIdsByStationIds != null)
				// carsharingConnector.connectForRealTimeData(cityUIDs,
				// vehicleIdsByStationIds, apiClient);
				System.err.println("REAL TIME");
			else {
				vehicleIdsByStationIds = carsharingConnector.connectForStaticData();
				// carsharingConnector.connectForRealTimeData(cityUIDs,
				// vehicleIdsByStationIds, apiClient);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Real Time Task finished");
	}
}