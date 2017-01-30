package it.bz.idm.carsharing.api;

import it.bz.tis.integreen.dto.carsharing.CarsharingStationDto;

public class ListStationsByBoundingBoxResponse {

	private CarsharingStationDto[] station;

	public CarsharingStationDto[] getStation() {
		return station;
	}

	public void setStation(CarsharingStationDto[] station) {
		this.station = station;
	} 

}
