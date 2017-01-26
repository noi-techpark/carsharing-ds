package it.bz.idm.carsharing;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.bz.idm.carsharing.dto.CarsharingStationDto;
import it.bz.idm.carsharing.dto.CarsharingVehicleDto;

/**
 * class for connecting to the carsharing-platform, to get and push data
 * @author sdalvai
 *
 */
@Component
public class CarsharingConnector {
	final static String apiUrl = "";

	public CarsharingConnector() {
		
	}
	
	public String connect(){
		RestTemplate restTemplate = new RestTemplate();
		
		Quote response = restTemplate.getForObject(
				"http://gturnquist-quoters.cfapps.io/api/random",Quote.class);
		return response.toString();
	}
	
	public List<CarsharingStationDto> getCarsharingStations(){
		List<CarsharingStationDto> ret = null;
		
		return ret;
	}
	
	public List<CarsharingVehicleDto> getCarsharingVehicles(){
		List<CarsharingVehicleDto> ret = null;
		
		return ret;
	}
	
}
