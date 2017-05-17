package it.bz.idm.carsharing.dto;

public class MyGetStationResponse{
	
	MyCarsharingStationDto[] carsharingStationDto;

	   public void setStation(MyCarsharingStationDto[] station)
	   {
	      this.carsharingStationDto = station;
	   }

	   public MyCarsharingStationDto[] getStation()
	   {
	      return this.carsharingStationDto;
	   }
}
