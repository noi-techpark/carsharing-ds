package it.bz.idm.carsharing.dto;

public class MyGetVehicleResponse {
	MyCarsharingVehicleDto[] carsharingVehicleDto;

	public void setVehicle(MyCarsharingVehicleDto[] vehicle) {
		this.carsharingVehicleDto = vehicle;
	}

	public MyCarsharingVehicleDto[] getVehicle() {
		return this.carsharingVehicleDto;
	}
}
