package it.bz.idm.carsharing.dto;

public class MyListVehiclesByStationResponse {
	public static class StationAndVehicles {
		MyCarsharingStationDto station;
		MyCarsharingVehicleDto[] vehicle;

		public MyCarsharingStationDto getStation() {
			return this.station;
		}

		public MyCarsharingVehicleDto[] getVehicle() {
			return this.vehicle;
		}
	}

	StationAndVehicles[] stationAndVehicles;

	public StationAndVehicles[] getStationAndVehicles() {
		return this.stationAndVehicles;
	}
}
