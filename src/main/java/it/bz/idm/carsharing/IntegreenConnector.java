package it.bz.idm.carsharing;

import java.util.List;

import it.bz.idm.carsharing.dto.CarsharingStationDto;
import it.bz.idm.carsharing.dto.CarsharingVehicleDto;

/**
 * class for connecting to the integreen-platform, to get and push data
 * 
 * @author sdalvai
 *
 */
public class IntegreenConnector {
	public IntegreenConnector() {

	}

	public void connect() {

	}

	public List<CarsharingStationDto> getCarsharingStations() {
		List<CarsharingStationDto> ret = null;

		return ret;
	}

	public List<CarsharingVehicleDto> getCarsharingVehicles() {
		List<CarsharingVehicleDto> ret = null;

		return ret;
	}

	public Boolean pushCarsharingStations(List<CarsharingStationDto> carsharingStations) {
		Boolean ret = false;

		return ret;
	}

	public Boolean pushCarsharingVehicles(List<CarsharingVehicleDto> carsharingVehicles) {
		Boolean ret = false;

		return ret;
	}
	
	public Boolean removeCarsharingVehicles(List<CarsharingVehicleDto> carsharingVehicles) {
		Boolean ret = false;

		return ret;
	}
	
	public Boolean removeCarsharingStations(List<CarsharingStationDto> carsharingStations) {
		Boolean ret = false;

		return ret;
	}
}
