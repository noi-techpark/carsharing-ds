package it.bz.idm.carsharing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.bz.idm.carsharing.api.CarsharingStationDto;
import it.bz.idm.carsharing.api.CarsharingVehicleDto;
import it.bz.idm.carsharing.api.GetStationRequest;
import it.bz.idm.carsharing.api.GetStationResponse;
import it.bz.idm.carsharing.api.GetVehicleRequest;
import it.bz.idm.carsharing.api.GetVehicleResponse;
import it.bz.idm.carsharing.api.ListStationsByCityRequest;
import it.bz.idm.carsharing.api.ListStationsByCityResponse;
import it.bz.idm.carsharing.api.ListVehiclesByStationsRequest;
import it.bz.idm.carsharing.api.ListVehiclesByStationsResponse;
import it.bz.idm.carsharing.api.ListVehiclesByStationsResponse.StationAndVehicles;

/**
 * class for connecting to the carsharing-platform, get the data abd push them
 * to the inegreenPlatform
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

	public HashMap<String, String[]> connectForStaticData(String[] cityUIDs) {
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

		GetStationResponse stationDetails = null;
		if (stationIds != null) {
			RequestEntity<GetStationRequest> getStationsRequest = new RequestEntity<GetStationRequest>(
					new GetStationRequest(stationIds), HttpMethod.GET, uri);
			ResponseEntity<GetStationResponse> getStationsResponse = null;
			if (getStationsRequest != null) {
				getStationsResponse = restTemplate.exchange(getStationsRequest, GetStationResponse.class);
			}

			if (getStationsResponse != null) {
				stationDetails = getStationsResponse.getBody();
			}
		}

		// get vehicles by stations

		RequestEntity<ListVehiclesByStationsRequest> listVehicleByStationsRequest = new RequestEntity<ListVehiclesByStationsRequest>(
				new ListVehiclesByStationsRequest(stationIds), HttpMethod.GET, uri);

		ResponseEntity<ListVehiclesByStationsResponse> listVehicleByStationsResponse = restTemplate
				.exchange(listVehicleByStationsRequest, ListVehiclesByStationsResponse.class);

		ListVehiclesByStationsResponse vehiclesByStations = null;
		if (listStationsByCityResponse != null)
			vehiclesByStations = listVehicleByStationsResponse.getBody();

		// prepare vehicle details

		HashMap<String, String[]> vehicleIdsByStationIds = new HashMap<>();
		ArrayList<String> vehicleIds = new ArrayList<String>();
		for (StationAndVehicles stationVehicles : vehiclesByStations.getStationAndVehicles()) {
			String[] tempVehicleIds = new String[stationVehicles.getVehicle().length];
			vehicleIdsByStationIds.put(stationVehicles.getStation().getId(), tempVehicleIds);
			for (int i = 0; i < stationVehicles.getVehicle().length; i++) {
				CarsharingVehicleDto carsharingVehicleDto = stationVehicles.getVehicle()[i];
				vehicleIds.add(carsharingVehicleDto.getId());
				// vehicleIds[i] = carsharingVehicleDto.getId();
			}
		}

		// get vehicle details

		RequestEntity<GetVehicleRequest> getvehiclerequest = new RequestEntity<GetVehicleRequest>(
				new GetVehicleRequest(vehicleIds.toArray(new String[0])), HttpMethod.GET, uri);

		ResponseEntity<GetVehicleResponse> getVehicleResponse = restTemplate.exchange(getvehiclerequest,
				GetVehicleResponse.class);

		GetVehicleResponse vehiclesDetails = null;
		if (getVehicleResponse != null)
			vehiclesDetails = getVehicleResponse.getBody();

		// write vehicel and station details to integreenPlatform

		// TODO IXMLRPCPusher missing

		// IXMLRPCPusher xmlrpcPusher;
		// Object result =
		// xmlrpcPusher.syncStations(CARSHARINGSTATION_DATASOURCE,
		// responseGetStation.getStation());
		// if (result instanceof IntegreenException)
		// {
		// throw new IOException("IntegreenException");
		// }
		//
		// synchronized (lock)
		// {
		// activityLog.report += "syncStations("
		// + CARSHARINGSTATION_DATASOURCE
		// + "): "
		// + responseGetStation.getStation().length
		// + "\n";
		// }
		//
		// result = xmlrpcPusher.syncStations(CARSHARINGCAR_DATASOURCE,
		// responseVehicleDetails.getVehicle());
		// if (result instanceof IntegreenException)
		// {
		// throw new IOException("IntegreenException");
		// }
		//
		// synchronized (lock)
		// {
		// activityLog.report += "syncStations("
		// + CARSHARINGCAR_DATASOURCE
		// + "): "
		// + responseVehicleDetails.getVehicle().length
		// + "\n";
		// }
		return vehicleIdsByStationIds;
	}

	public void connectForRealTimeData(String[] cityUIDs) {
		
		
		

		// /**
		// * STATIONS
		// */
		//
		// // for new stations
		// List<CarsharingStationDto> newCarsharingStations = new ArrayList<>();
		//
		// for (int i = 0; i < apiCarsharingStations.size(); i++) {
		// // don't know if they are sorted
		// if (integreenCarsharingStations.get(i) != null)
		// if
		// (!apiCarsharingStations.get(i).getId().equals(integreenCarsharingStations.get(i).getId()))
		// newCarsharingStations.add(apiCarsharingStations.get(0));
		// else
		// oldIntegreenCarsharingStation.remove(integreenCarsharingStations.get(i));
		// }
		//
		// if (oldIntegreenCarsharingStation.size() > 0) {
		// // the stations of this list are no longer available
		// integreenConnector.removeCarsharingStations(oldIntegreenCarsharingStation);
		// }
		//
		// if (newCarsharingStations.size() > 0)
		// // new stations are available
		// integreenConnector.pushCarsharingStations(newCarsharingStations);
		//
		// /**
		// * CARS
		// */
		// // for new cars
		// List<CarsharingVehicleDto> newCarsharingVehicles = new ArrayList<>();
		//
		// for (int i = 0; i < apiCarsharingVehicles.size(); i++) {
		// // don't know if they are sorted
		// if (integreenCarsharingVehicles.get(i) != null)
		// if
		// (!apiCarsharingVehicles.get(i).getId().equals(integreenCarsharingVehicles.get(i).getId()))
		// newCarsharingVehicles.add(apiCarsharingVehicles.get(0));
		// else
		// oldIntegreenCarsharingVehicle.remove(integreenCarsharingVehicles.get(i));
		// }
		//
		// if (oldIntegreenCarsharingVehicle.size() > 0) {
		// // the stations of this list are no longer available
		// integreenConnector.removeCarsharingVehicles(oldIntegreenCarsharingVehicle);
		// }
		//
		// if (newCarsharingVehicles.size() > 0)
		// // new stations are available
		// integreenConnector.pushCarsharingVehicles(newCarsharingVehicles);

	}

}
