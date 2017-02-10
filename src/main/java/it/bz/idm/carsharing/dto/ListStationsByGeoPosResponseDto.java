package it.bz.idm.carsharing.dto;

import java.util.List;

import it.bz.idm.carsharing.wsdl.HalWarnings;
import it.bz.idm.carsharing.wsdl.ListStationsByCityResponse;
import it.bz.idm.carsharing.wsdl.Station;

public class ListStationsByGeoPosResponseDto {

	protected List<Station> station;

	public List<Station> getStation() {
		return station;
	}

	public void setStation(List<Station> station) {
		this.station = station;
	}


}
