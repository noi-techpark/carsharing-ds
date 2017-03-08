package it.bz.idm.carsharing.dto;

import it.bz.idm.bdp.dto.carsharing.CarsharingStationDto;
import it.bz.idm.bdp.dto.carsharing.CarsharingVehicleDto;

public class MyListVehiclesByStationResponse {
	public static class StationAndVehicles {
		CarsharingStationDto station;
		CarsharingVehicleDto[] vehicle;

		public CarsharingStationDto getStation() {
			return this.station;
		}

		public CarsharingVehicleDto[] getVehicle() {
			return this.vehicle;
		}
	}

	StationAndVehicles[] stationAndVehicles;

	public StationAndVehicles[] getStationAndVehicles() {
		return this.stationAndVehicles;
	}
}
