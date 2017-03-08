package it.bz.idm.carsharing.dto;

import it.bz.idm.bdp.dto.carsharing.CarsharingVehicleDto;

public class MyGetVehicleResponse {
	CarsharingVehicleDto[] carsharingVehicleDto;

	public void setVehicle(CarsharingVehicleDto[] vehicle) {
		this.carsharingVehicleDto = vehicle;
	}

	public CarsharingVehicleDto[] getVehicle() {
		return this.carsharingVehicleDto;
	}
}
