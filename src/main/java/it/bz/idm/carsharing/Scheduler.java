package it.bz.idm.carsharing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.bz.idm.bdp.dto.DataTypeDto;
import it.bz.idm.bdp.util.IntegreenException;

@Component
public class Scheduler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private HashMap<String, List<String>> vehicleIdsByStationIds;

	@Autowired
	private CarsharingConnector carsharingConnector;

	@Autowired
	CarsharingStationSync stationPusher;

	@Autowired
	CarsharingCarSync carPusher;


	/**
	 * for getting the static data like vehicle and stationlist from the
	 * carsharingAPI and push them to te integreen-platform
	 */
	@Scheduled(cron = "0 0 0 * * *") // every day at midnight
	public void staticTask() {
		try {
			vehicleIdsByStationIds = carsharingConnector.connectForStaticData(stationPusher, carPusher);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * for getting real time data like the vehicle and stationlist from the
	 * carsharingAPI and push them to te integreen-platform
	 */
	@Scheduled(cron = "0 0/10 * * * ?") // every 10 minutes, but at 6.10 PM
	public void realTimeTask() {
		try {
			if (vehicleIdsByStationIds != null) {
				carsharingConnector.connectForRealTimeData(vehicleIdsByStationIds, stationPusher, carPusher);
			} else {
				logger.info("Get Static Data for the first Time");
				vehicleIdsByStationIds = carsharingConnector.connectForStaticData(stationPusher, carPusher);
				logger.info("Get Static Data finished for the first Time");
				logger.info("Real Time Task for the first Time");
				carsharingConnector.connectForRealTimeData(vehicleIdsByStationIds, stationPusher, carPusher);
				logger.info("Real Time Task finished for the first Time");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// gets called after Spring context initialization to sync the data types
	@PostConstruct
	public void syncDataTypes() throws IOException {
		DataTypeDto availibilityDataTypeDto = new DataTypeDto();
		availibilityDataTypeDto.setDescription("Carsharing Car avalibility");
		availibilityDataTypeDto.setName(DataTypeDto.AVAILABILITY);

		DataTypeDto futureAvailibilityDataTypeDto = new DataTypeDto();
		futureAvailibilityDataTypeDto.setDescription("Carsharing Car future-avalibility");
		futureAvailibilityDataTypeDto.setName(DataTypeDto.FUTURE_AVAILABILITY);

		DataTypeDto numberAvailableDataTypeDto = new DataTypeDto();
		numberAvailableDataTypeDto.setDescription("Carsharing Station number-available");
		numberAvailableDataTypeDto.setName(DataTypeDto.NUMBER_AVAILABE);

		Object syncStaionDataTypes = stationPusher.syncDataTypes(new Object[] { numberAvailableDataTypeDto });
		if (syncStaionDataTypes instanceof IntegreenException)
			throw new IOException("IntegreenException: station dataType syncing");
		Object syncCarDataTypes = carPusher
				.syncDataTypes(new Object[] { availibilityDataTypeDto, futureAvailibilityDataTypeDto });
		if (syncCarDataTypes instanceof IntegreenException)
			throw new IOException("IntegreenException: car dataType syncing");
		logger.info("Data Types sync finished");
	}
}