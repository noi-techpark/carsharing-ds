package it.bz.idm.carsharing;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.bz.idm.carsharing.api.ApiClient;
import it.bz.idm.carsharing.api.CarsharingStationDto;
import it.bz.idm.carsharing.api.CarsharingVehicleDto;
import it.bz.idm.carsharing.api.GetStationRequest;
import it.bz.idm.carsharing.api.GetStationResponse;
import it.bz.idm.carsharing.api.GetVehicleRequest;
import it.bz.idm.carsharing.api.GetVehicleResponse;
import it.bz.idm.carsharing.api.ListStationsByCityRequest;
import it.bz.idm.carsharing.api.ListStationsByCityResponse;
import it.bz.idm.carsharing.api.ListVehicleOccupancyByStationRequest;
import it.bz.idm.carsharing.api.ListVehicleOccupancyByStationResponse;
import it.bz.idm.carsharing.api.ListVehicleOccupancyByStationResponse.VehicleAndOccupancies;
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
	static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	final static long INTERVALL = 10L * 60L * 1000L;
	public static final String CARSHARINGSTATION_DATASOURCE = "Carsharingstation";
	public static final String CARSHARINGCAR_DATASOURCE = "Carsharingcar";
	private final Logger logger = LoggerFactory.getLogger(CarsharingConnector.class);

	public CarsharingConnector() {
		try {
			uri = new URI(API_URL);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HashMap<String, String[]> connectForStaticData(String[] cityUIDs, ApiClient apiClient) throws IOException {
		// RestTemplate restTemplate = new RestTemplate();

		// get stations by city

		ListStationsByCityRequest request = new ListStationsByCityRequest(cityUIDs);
		ListStationsByCityResponse response = apiClient.callWebService(request, ListStationsByCityResponse.class);
		CarsharingStationDto[] stations = null;
		if (response.getCityAndStations() != null)
			stations = response.getCityAndStations()[0].getStation();

		// RequestEntity<ListStationsByCityRequest> requestEntity = new
		// RequestEntity<ListStationsByCityRequest>(
		// new ListStationsByCityRequest(cityUIDs), HttpMethod.GET, uri);
		// ResponseEntity<ListStationsByCityResponse> listStationsByCityResponse
		// = null;
		// // not correct yet
		// if (requestEntity != null) {
		// listStationsByCityResponse = restTemplate.exchange(requestEntity,
		// ListStationsByCityResponse.class);
		// }
		// CarsharingStationDto[] stations = null;
		// if (listStationsByCityResponse != null) {
		// stations =
		// listStationsByCityResponse.getBody().getCityAndStations()[0].getStation();
		// }

		// get stattion details
		String[] stationIds = null;
		if (stations != null) {
			stationIds = new String[stations.length];
			for (int i = 0; i < stations.length; i++) {
				stationIds[i] = stations[i].getId();
			}
		}

		GetStationResponse stationDetails = null;
		if (stationIds != null) {
			RequestEntity<GetStationRequest> getStationsRequest = new RequestEntity<GetStationRequest>(
					new GetStationRequest(stationIds), HttpMethod.GET, uri);

			// TODO check if this is correct
			// HttpEntity<GetStationRequest> entity = new
			// HttpEntity<GetStationRequest>(new
			// GetStationRequest(stationIds),new HttpHeaders());

			// ResponseEntity<GetStationResponse> getStationsResponse = null;
			// if (getStationsRequest != null) {
			// getStationsResponse = restTemplate.exchange(getStationsRequest,
			// GetStationResponse.class);
			// }
			GetStationRequest requestGetStation = new GetStationRequest(stationIds);
			GetStationResponse responseGetStation = null;
			responseGetStation = apiClient.callWebService(requestGetStation, GetStationResponse.class);

			// if (getStationsResponse != null) {
			// stationDetails = getStationsResponse.getBody();
			// }

			logger.error(responseGetStation.toString());
		}

		// get vehicles by stations

		// RequestEntity<ListVehiclesByStationsRequest>
		// listVehicleByStationsRequest = new
		// RequestEntity<ListVehiclesByStationsRequest>(
		// new ListVehiclesByStationsRequest(stationIds), HttpMethod.GET, uri);
		//
		// ResponseEntity<ListVehiclesByStationsResponse>
		// listVehicleByStationsResponse = restTemplate
		// .exchange(listVehicleByStationsRequest,
		// ListVehiclesByStationsResponse.class);
		//
		// ListVehiclesByStationsResponse vehiclesByStations = null;
		// if (listStationsByCityResponse != null)
		// vehiclesByStations = listVehicleByStationsResponse.getBody();

		ListVehiclesByStationsRequest vehicles = new ListVehiclesByStationsRequest(stationIds);
		ListVehiclesByStationsResponse responseVehicles = null;
		responseVehicles = apiClient.callWebService(vehicles, ListVehiclesByStationsResponse.class);

		// prepare vehicle details

		HashMap<String, String[]> vehicleIdsByStationIds = new HashMap<>();
		ArrayList<String> vehicleIds = new ArrayList<String>();
		for (StationAndVehicles stationVehicles : responseVehicles.getStationAndVehicles()) {
			String[] tempVehicleIds = new String[stationVehicles.getVehicle().length];
			vehicleIdsByStationIds.put(stationVehicles.getStation().getId(), tempVehicleIds);
			for (int i = 0; i < stationVehicles.getVehicle().length; i++) {
				CarsharingVehicleDto carsharingVehicleDto = stationVehicles.getVehicle()[i];
				vehicleIds.add(carsharingVehicleDto.getId());
				// vehicleIds[i] = carsharingVehicleDto.getId();
			}
		}

		// get vehicle details

		// RequestEntity<GetVehicleRequest> getvehiclerequest = new
		// RequestEntity<GetVehicleRequest>(
		// new GetVehicleRequest(vehicleIds.toArray(new String[0])),
		// HttpMethod.GET, uri);
		//
		// ResponseEntity<GetVehicleResponse> getVehicleResponse =
		// restTemplate.exchange(getvehiclerequest,
		// GetVehicleResponse.class);
		//
		// GetVehicleResponse vehiclesDetails = null;
		// if (getVehicleResponse != null)
		// vehiclesDetails = getVehicleResponse.getBody();

		GetVehicleRequest requestVehicleDetails = new GetVehicleRequest(vehicleIds.toArray(new String[0]));
		GetVehicleResponse responseVehicleDetails = apiClient.callWebService(requestVehicleDetails,
				GetVehicleResponse.class);

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

	public void connectForRealTimeData(String[] cityUIDs, HashMap<String, String[]> vehicleIdsByStationIds,
			ApiClient apiClient) {

		RestTemplate restTemplate = new RestTemplate();

		///////////////////////////////////////////////////////////////
		// Read vehicles occupancy and calculate summaries
		///////////////////////////////////////////////////////////////

		// TODO des isch a schmorn
		Long updateTime = new Date().getTime();
		String created = String.valueOf(updateTime);

		// Current and forecast
		for (long forecast : new long[] { 0, 30L * 60L * 1000L }) {
			ArrayList<HashMap<String, String>> stationOccupancies = new ArrayList<>();
			ArrayList<HashMap<String, String>> vehicleOccupancies = new ArrayList<>();

			String begin = String.valueOf(updateTime + forecast);
			// TODO begin buffer depends on car type
			String begin_carsharing = SIMPLE_DATE_FORMAT.format(new Date(updateTime - 30L * 60L * 1000L + forecast));
			String end = SIMPLE_DATE_FORMAT.format(new Date(updateTime + INTERVALL + forecast));

			String[] stationIds = vehicleIdsByStationIds.keySet().toArray(new String[0]);
			Arrays.sort(stationIds);

			for (String stationId : stationIds) {
				String[] vehicleIds = vehicleIdsByStationIds.get(stationId);
				ListVehicleOccupancyByStationRequest occupancyByStationRequest = new ListVehicleOccupancyByStationRequest(
						begin_carsharing, end, stationId, vehicleIds);

				RequestEntity<ListVehicleOccupancyByStationRequest> listVehicleOccupancyByStationrequest = new RequestEntity<ListVehicleOccupancyByStationRequest>(
						occupancyByStationRequest, HttpMethod.GET, uri);

				ResponseEntity<ListVehicleOccupancyByStationResponse> listVehicleOccupancyBySationResponse = restTemplate
						.exchange(listVehicleOccupancyByStationrequest, ListVehicleOccupancyByStationResponse.class);

				ListVehicleOccupancyByStationResponse responseOccupancy = listVehicleOccupancyBySationResponse
						.getBody();

				VehicleAndOccupancies[] occupancies = responseOccupancy.getVehicleAndOccupancies();
				if (occupancies.length != vehicleIds.length) // Same number of
																// responses as
																// the number to
																// requests
				{
					throw new IllegalStateException();
				}
				int free = 0;
				for (VehicleAndOccupancies vehicleOccupancy : occupancies) {
					if (vehicleOccupancy.getOccupancy().length > 1) {
						throw new IllegalStateException("Why???");
					}
					int state = 0; // free
					if (vehicleOccupancy.getOccupancy().length == 1) {
						state = 1;
					} else {
						free++;
					}
					HashMap<String, String> vehicleData = new HashMap<String, String>();
					vehicleData.put(CarsharingVehicleDto.IDENTIFIER, vehicleOccupancy.getVehicle().getId());
					vehicleData.put(CarsharingVehicleDto.STATE, String.valueOf(state));
					vehicleData.put(CarsharingVehicleDto.TIMESTAMP, begin);
					vehicleData.put(CarsharingVehicleDto.CREATED_ON, created);
					vehicleOccupancies.add(vehicleData);
				}

				HashMap<String, String> stationData = new HashMap<String, String>();
				stationData.put(CarsharingStationDto.IDENTIFIER, stationId);
				stationData.put(CarsharingStationDto.VALUE_IDENTIFIER, String.valueOf(free));
				stationData.put(CarsharingStationDto.TIMESTAMP, begin);
				stationData.put(CarsharingStationDto.CREATED_ON, created);
				stationOccupancies.add(stationData);

			}

			///////////////////////////////////////////////////////////////
			// Write data to integreen
			///////////////////////////////////////////////////////////////

			// TODO XMLRPCPUSHER MISSING

			// Object[] stationArray = stationOccupancies.toArray();
			// Object result =
			// xmlrpcPusher.pushData(CARSHARINGSTATION_DATASOURCE,
			// stationArray);
			// if (result instanceof IntegreenException) {
			// throw new IOException("IntegreenException");
			// }
			// synchronized (lock) {
			// activityLog.report += "pushData(" + CARSHARINGSTATION_DATASOURCE
			// + "): " + stationArray.length + "\n";
			// }
			// Object[] vehicleArray = vehicleOccupancies.toArray();
			// result = xmlrpcPusher.pushData(CARSHARINGCAR_DATASOURCE,
			// vehicleArray);
			// if (result instanceof IntegreenException) {
			// throw new IOException("IntegreenException");
			// }
			// synchronized (lock) {
			// activityLog.report += "pushData(" + CARSHARINGCAR_DATASOURCE +
			// "): " + vehicleArray.length + "\n";
			// }
		}

		/**
		 * old
		 */
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
