package it.bz.idm.carsharing;



import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.bz.idm.carsharing.dto.CarsharingStationDto;
import it.bz.idm.carsharing.dto.CarsharingVehicleDto;

@Component
public class Scheduler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private IntegreenConnector integreenConnector = new IntegreenConnector();
	private CarsharingConnector carsharingConnector = new CarsharingConnector();

	/**
	 * for getting the static data like vehicle and stationlist from the
	 * carsharingAPI and push them to te integreen-platform
	 */
	@Scheduled(cron = "0 0 0 * * *") // every day at midnight
	public void staticTask() {
		logger.info("Static Task started");

		carsharingConnector.connect();
		List<CarsharingStationDto> newCarsharingStations = carsharingConnector.getCarsharingStations();
		List<CarsharingVehicleDto> newCarsharingVehicles = carsharingConnector.getCarsharingVehicles();
		
		for(CarsharingVehicleDto car: newCarsharingVehicles){
		}
		
		/**
		 * forecast ???
		 */

		logger.info("Static Task finished");
	}

	/**
	 * for getting real time data like the vehicle and stationlist from the
	 * carsharingAPI and push them to te integreen-platform
	 */
	@Scheduled(fixedRate = 600000) // 10 minutes interval
	public void realTimeTask() {
		logger.info("Real Time Task");

		integreenConnector.connect();
		List<CarsharingStationDto> integreenCarsharingStations = integreenConnector.getCarsharingStations();
		List<CarsharingVehicleDto> integreenCarsharingVehicles = integreenConnector.getCarsharingVehicles();
		
		
		//not correct: must make deepCopy
//		List<CarsharingStationDto> oldIntegreenCarsharingStation = new ArrayList<>(integreenCarsharingStations);
//		List<CarsharingVehicleDto> oldIntegreenCarsharingVehicle = new ArrayList<>(integreenCarsharingVehicles);

		String response = carsharingConnector.connect();
		
		logger.error(response);
//		
//		List<CarsharingStationDto> apiCarsharingStations = carsharingConnector.getCarsharingStations();
//		List<CarsharingVehicleDto> apiCarsharingVehicles = carsharingConnector.getCarsharingVehicles();
//
//		/**
//		 * STATIONS
//		 */
//
//		// for new stations
//		List<CarsharingStationDto> newCarsharingStations = new ArrayList<>();
//
//		for (int i = 0; i < apiCarsharingStations.size(); i++) {
//			// don't know if they are sorted
//			if (integreenCarsharingStations.get(i) != null)
//				if (!apiCarsharingStations.get(i).getId().equals(integreenCarsharingStations.get(i).getId()))
//					newCarsharingStations.add(apiCarsharingStations.get(0));
//				else
//					oldIntegreenCarsharingStation.remove(integreenCarsharingStations.get(i));
//		}
//
//		if (oldIntegreenCarsharingStation.size() > 0) {
//			// the stations of this list are no longer available
//			integreenConnector.removeCarsharingStations(oldIntegreenCarsharingStation);
//		}
//
//		if (newCarsharingStations.size() > 0)
//			// new stations are available
//			integreenConnector.pushCarsharingStations(newCarsharingStations);
//
//		
//		/**
//		 * CARS
//		 */
//		// for new cars
//		List<CarsharingVehicleDto> newCarsharingVehicles = new ArrayList<>();
//
//		for (int i = 0; i < apiCarsharingVehicles.size(); i++) {
//			// don't know if they are sorted
//			if (integreenCarsharingVehicles.get(i) != null)
//				if (!apiCarsharingVehicles.get(i).getId().equals(integreenCarsharingVehicles.get(i).getId()))
//					newCarsharingVehicles.add(apiCarsharingVehicles.get(0));
//				else
//					oldIntegreenCarsharingVehicle.remove(integreenCarsharingVehicles.get(i));
//		}
//
//		if (oldIntegreenCarsharingVehicle.size() > 0) {
//			// the stations of this list are no longer available
//			integreenConnector.removeCarsharingVehicles(oldIntegreenCarsharingVehicle);
//		}
//
//		if (newCarsharingVehicles.size() > 0)
//			// new stations are available
//			integreenConnector.pushCarsharingVehicles(newCarsharingVehicles);

	}
}