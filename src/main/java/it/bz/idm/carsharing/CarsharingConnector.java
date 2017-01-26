package it.bz.idm.carsharing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.bz.idm.carsharing.api.CarsharingStationDto;
import it.bz.idm.carsharing.api.CarsharingVehicleDto;
import it.bz.idm.carsharing.api.GetStationRequest;
import it.bz.idm.carsharing.api.GetStationResponse;
import it.bz.idm.carsharing.api.ListStationsByCityRequest;
import it.bz.idm.carsharing.api.ListStationsByCityResponse;
import it.bz.idm.carsharing.api.ListVehiclesByStationsRequest;
import it.bz.idm.carsharing.api.ListVehiclesByStationsResponse;
import it.bz.idm.carsharing.api.ListVehiclesByStationsResponse.StationAndVehicles;

/**
 * class for connecting to the carsharing-platform, to get and push data
 * 
 * @author sdalvai
 *
 */
@Component
public class CarsharingConnector {
	final static String API_URL = "https://xml.dbcarsharing-buchung.de/hal2_api/hal2_api_3.php?protocol=json";

	private URI uri = null;

	public CarsharingConnector() {
		try {
			uri = new URI(API_URL);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String connect(String[] cityUIDs) {
		RestTemplate restTemplate = new RestTemplate();

		// get stations by city
		RequestEntity<ListStationsByCityRequest> requestEntity = new RequestEntity<ListStationsByCityRequest>(
				new ListStationsByCityRequest(cityUIDs), HttpMethod.GET, uri);
		ResponseEntity<ListStationsByCityResponse> listStationsByCityResponse = null;
		// not correct yet
		if (requestEntity != null) {
			listStationsByCityResponse = restTemplate.exchange(requestEntity, ListStationsByCityResponse.class);
		}
		CarsharingStationDto[] stations = null;
		if (listStationsByCityResponse != null) {
			stations = listStationsByCityResponse.getBody().getCityAndStations()[0].getStation();
		}

		// get stattion details
		String[] stationIds = new String[stations.length];
		if (stations != null) {
			for (int i = 0; i < stations.length; i++) {
				stationIds[i] = stations[i].getId();
			}
		}

		if (stationIds != null) {
			RequestEntity<GetStationRequest> getStationsRequest = new RequestEntity<GetStationRequest>(
					new GetStationRequest(stationIds), HttpMethod.GET, uri);
			ResponseEntity<GetStationResponse> getStationsResponse = null;
			if (getStationsRequest != null) {
				getStationsResponse = restTemplate.exchange(getStationsRequest, GetStationResponse.class);
			}

			if (getStationsResponse != null) {
				// write data to integreen
			}
		}

		// get vehicles by stations

		RequestEntity<ListVehiclesByStationsRequest> listVehicleByStationsRequest = new RequestEntity<ListVehiclesByStationsRequest>(
				new ListVehiclesByStationsRequest(stationIds), HttpMethod.GET, uri);

		ResponseEntity<ListVehiclesByStationsResponse> listVehicleByStationsResponse = restTemplate
				.exchange(listVehicleByStationsRequest, ListVehiclesByStationsResponse.class);
		
		ListVehiclesByStationsResponse vehiclesByStations = null;
		if(listStationsByCityResponse != null)
			vehiclesByStations = listVehicleByStationsResponse.getBody();
			
		
		// vehicle details

		HashMap<String, String[]> vehicleIdsByStationIds = new HashMap<>();
		ArrayList<String> veichleIds = new ArrayList<String>();
		for (StationAndVehicles stationVehicles : vehiclesByStations.getStationAndVehicles()) {
			String[] vehicleIds = new String[stationVehicles.getVehicle().length];
			vehicleIdsByStationIds.put(stationVehicles.getStation().getId(), vehicleIds);
			for (int i = 0; i < stationVehicles.getVehicle().length; i++) {
				CarsharingVehicleDto carsharingVehicleDto = stationVehicles.getVehicle()[i];
				veichleIds.add(carsharingVehicleDto.getId());
				vehicleIds[i] = carsharingVehicleDto.getId();
			}
		}

		Quote response = restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
		return response.toString();
	}

	public List<CarsharingStationDto> getCarsharingStations() {
		List<CarsharingStationDto> ret = null;

		return ret;
	}

	public List<CarsharingVehicleDto> getCarsharingVehicles() {
		List<CarsharingVehicleDto> ret = null;

		return ret;
	}

}
