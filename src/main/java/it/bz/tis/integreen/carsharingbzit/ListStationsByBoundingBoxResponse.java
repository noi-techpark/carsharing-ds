package it.bz.tis.integreen.carsharingbzit;

import it.bz.idm.bdp.dto.carsharing.CarsharingStationDto;

public class ListStationsByBoundingBoxResponse {

	private CarsharingStationDto[] station;

	public CarsharingStationDto[] getStation() {
		return station;
	}

	public void setStation(CarsharingStationDto[] station) {
		this.station = station;
	} 

}
