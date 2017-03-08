package it.bz.idm.carsharing.dto;

import it.bz.idm.bdp.dto.carsharing.CarsharingStationDto;

public class MyGetStationResponse{
	
	CarsharingStationDto[] carsharingStationDto;

	   public void setStation(CarsharingStationDto[] station)
	   {
	      this.carsharingStationDto = station;
	   }

	   public it.bz.idm.bdp.dto.carsharing.CarsharingStationDto[] getStation()
	   {
	      return this.carsharingStationDto;
	   }
}
