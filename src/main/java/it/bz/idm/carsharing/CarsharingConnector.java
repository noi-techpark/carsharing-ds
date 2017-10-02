package it.bz.idm.carsharing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.bz.idm.bdp.dto.DataMapDto;
import it.bz.idm.bdp.dto.DataTypeDto;
import it.bz.idm.bdp.dto.SimpleRecordDto;
import it.bz.idm.bdp.dto.carsharing.CarsharingStationDto;
import it.bz.idm.bdp.dto.carsharing.CarsharingVehicleDto;
import it.bz.idm.bdp.util.IntegreenException;
import it.bz.idm.carsharing.dto.MyCarsharingStationDto;
import it.bz.idm.carsharing.dto.MyCarsharingVehicleDto;
import it.bz.idm.carsharing.dto.MyGetStationRequest;
import it.bz.idm.carsharing.dto.MyGetStationResponse;
import it.bz.idm.carsharing.dto.MyGetVehicleRequest;
import it.bz.idm.carsharing.dto.MyGetVehicleResponse;
import it.bz.idm.carsharing.dto.MyListStationsByGeoPosRequest;
import it.bz.idm.carsharing.dto.MyListVehicleOccupancyByStationRequest;
import it.bz.idm.carsharing.dto.MyListVehicleOccupancyByStationResponse;
import it.bz.idm.carsharing.dto.MyListVehicleOccupancyByStationResponse.VehicleAndOccupancies;
import it.bz.idm.carsharing.dto.MyListVehiclesByStationRequest;
import it.bz.idm.carsharing.dto.MyListVehiclesByStationResponse;
import it.bz.idm.carsharing.dto.MyListVehiclesByStationResponse.StationAndVehicles;
import it.bz.idm.carsharing.wsdl.BoundingBox;
import it.bz.idm.carsharing.wsdl.GeoPos;
import it.bz.idm.carsharing.wsdl.ListStationsByGeoPosResponse;
import it.bz.idm.carsharing.wsdl.Station;
import it.bz.idm.carsharing.wsdl.UserAuth;

/**
 * class for connecting to the carsharing-platform, get the data and push them
 * to the inegreenPlatform
 * 
 * @author sdalvai
 *
 */

@Component
public class CarsharingConnector {
	final static long INTERVALL = 10L * 60L * 1000L;
	private final Logger logger = LoggerFactory.getLogger(CarsharingConnector.class);

	RestTemplate restTemplate;

	private String endpoint;
	private String user;
	private String password;

	private final String INTEGREEN_CAR = "Carsharingcar";
	private final String INTEGREEN_STATION = "Carsharingstation";

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private List<BoundingBox> boxes;

	protected UserAuth userAuth = null;

	ObjectMapper mapper;

	@Autowired
	public CarsharingConnector(@Value("${cred.endpoint}") final String endpoint,
			@Value("${cred.user}") final String user, @Value("${cred.password}") final String password) {

		this.endpoint = endpoint;
		this.user = user;
		this.password = password;

		userAuth = new UserAuth();
		userAuth.setUsername(user);
		userAuth.setPassword(password);

		boxes = new ArrayList<BoundingBox>();
		boxes.add(setUpBoundingBox(46.86113, 10.375214, 46.459147, 11.059799));
		boxes.add(setUpBoundingBox(46.765265, 11.015081, 46.450277, 11.555557));
		boxes.add(setUpBoundingBox(46.847924, 11.458354, 46.533418, 11.99883));
		boxes.add(setUpBoundingBox(46.455303, 11.166573, 46.218327, 11.521568));
		boxes.add(setUpBoundingBox(47.018653, 11.092758, 46.794448, 11.797256));
		boxes.add(setUpBoundingBox(47.098175, 11.959305, 46.598506, 12.423477));

		// halapi sends contenttype text/html instead of json. so we have to
		// register text/html to the message converter
		restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new RestTemplateErrorHandler());
		List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
		for (HttpMessageConverter<?> converter : converters) {
			if (converter instanceof MappingJackson2HttpMessageConverter) {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
				objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
				objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
				((MappingJackson2HttpMessageConverter) converter).setObjectMapper(objectMapper);
				try {
					List<MediaType> mediaTypes = new ArrayList<>();
					mediaTypes.add(MediaType.TEXT_HTML);
					mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
					((MappingJackson2HttpMessageConverter) converter).setSupportedMediaTypes(mediaTypes);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public HashMap<String, List<String>> connectForStaticData(JsonCarsharingPusher jsonCarsharingPusher)
			throws IOException {
		Long now = System.currentTimeMillis();
		logger.info("STATIC DATA STARTED AT " + now);

		List<Integer> stationIds = new ArrayList<>();
		for (BoundingBox box : boxes) {
			MyListStationsByGeoPosRequest byGeoPosRequest = new MyListStationsByGeoPosRequest(userAuth, box);

			ListStationsByGeoPosResponse geoPosResponse = restTemplate.postForObject(endpoint, byGeoPosRequest,
					ListStationsByGeoPosResponse.class);
			if (geoPosResponse != null)
				for (Station station : geoPosResponse.getStation())
					stationIds.add(station.getUid());
		}

		// Stations details
		MyGetStationRequest getStationRequest = new MyGetStationRequest(userAuth, stationIds);

		MyGetStationResponse stationDetailsResponse = restTemplate.postForObject(endpoint, getStationRequest,
				MyGetStationResponse.class);

		// Vehicles by stations
		MyListVehiclesByStationRequest vehicles = new MyListVehiclesByStationRequest(userAuth, stationIds);

		MyListVehiclesByStationResponse listVehiclesByStationResponse = restTemplate.postForObject(endpoint, vehicles,
				MyListVehiclesByStationResponse.class);

		// Vehicles details
		HashMap<String, List<String>> vehicleIdsByStationIds = new HashMap<>();
		List<String> vehicleIdsForDetailRequest = new ArrayList<String>();
		if (listVehiclesByStationResponse != null && listVehiclesByStationResponse.getStationAndVehicles() != null)
			for (StationAndVehicles stationAndVehicles : listVehiclesByStationResponse.getStationAndVehicles()) {
				// station and vehicles
				List<String> vehicleIds = new ArrayList<String>();
				vehicleIdsByStationIds.put(stationAndVehicles.getStation().getId(), vehicleIds);
				for (int i = 0; i < stationAndVehicles.getVehicle().length; i++) {
					vehicleIds.add(stationAndVehicles.getVehicle()[i].getId());
					vehicleIdsForDetailRequest.add(stationAndVehicles.getVehicle()[i].getId());
				}
			}
		// getDetails
		MyGetVehicleRequest getVehicleRequestDto = new MyGetVehicleRequest(userAuth, vehicleIdsForDetailRequest);
		MyGetVehicleResponse getVehicleResponse = restTemplate.postForObject(endpoint, getVehicleRequestDto,
				MyGetVehicleResponse.class);
		// Write data to integreen

		// prepare for json service

		// convert to StationDto
		CarsharingStationDto[] stationsDtos = MyCarsharingStationDto
				.convertToStationDto(stationDetailsResponse.getStation());
		CarsharingVehicleDto[] vehiclesDtos = MyCarsharingVehicleDto
				.convertToStationDto(getVehicleResponse.getVehicle());
		Object syncStations = jsonCarsharingPusher.syncStations(INTEGREEN_STATION, stationsDtos);
		if (syncStations instanceof IntegreenException)
			throw new IOException("IntegreenException: static Stations sync");
		Object syncCars = jsonCarsharingPusher.syncStations(INTEGREEN_CAR, vehiclesDtos);
		if (syncCars instanceof IntegreenException)
			throw new IOException("IntegreenException: static Cars sync");

		logger.info("STATIC DATA ENDED AFTER " + (System.currentTimeMillis() - now));
		return vehicleIdsByStationIds;
	}

	public void connectForRealTimeData(HashMap<String, List<String>> vehicleIdsByStationIds,
			JsonCarsharingPusher jsonCarsharingPusher) throws IOException {
		Long now = System.currentTimeMillis();
		logger.info("REAL TIME DATA STARTED AT " + now);

		for (long forecast : new long[] { 0, INTERVALL }) {
			// sets the timestamp so, that the seconds and miliseconds are 0
			Calendar calBegin = Calendar.getInstance();
			calBegin.setTimeInMillis(now + forecast);
			calBegin.set(Calendar.SECOND, 0);
			calBegin.set(Calendar.MILLISECOND, 0);
			Calendar calEnd = Calendar.getInstance();
			calEnd.setTimeInMillis(now + forecast + INTERVALL);
			calEnd.set(Calendar.SECOND, 0);
			calEnd.set(Calendar.MILLISECOND, 0);
			GregorianCalendar begin2 = new GregorianCalendar();
			begin2.setTime(calBegin.getTime());
			GregorianCalendar end2 = new GregorianCalendar();
			end2.setTime(calEnd.getTime());
			XMLGregorianCalendar begin = null;
			XMLGregorianCalendar end = null;

			try {
				begin = DatatypeFactory.newInstance().newXMLGregorianCalendar(begin2);
				end = DatatypeFactory.newInstance().newXMLGregorianCalendar(end2);
			} catch (DatatypeConfigurationException e1) {
				e1.printStackTrace();
			}
			String[] stationIds = vehicleIdsByStationIds.keySet().toArray(new String[0]);
			Arrays.sort(stationIds);
			DataMapDto stationData = new DataMapDto();
			DataMapDto vehicleData = new DataMapDto();
			for (String stationId : stationIds) {
				List<String> vehicleIds = vehicleIdsByStationIds.get(stationId);
				MyListVehicleOccupancyByStationRequest listVehicleOccupancyByStationRequestDto = new MyListVehicleOccupancyByStationRequest(
						userAuth, begin, end, stationId, vehicleIds.toArray(new String[0]));

				MyListVehicleOccupancyByStationResponse listVehicleOccupancyByStationResponse = restTemplate
						.postForObject(endpoint, listVehicleOccupancyByStationRequestDto,
								MyListVehicleOccupancyByStationResponse.class);
				Short freeVehicles = 0;

				// to avoid nullPointException
				if (listVehicleOccupancyByStationResponse != null
						&& listVehicleOccupancyByStationResponse.getVehicleAndOccupancies() != null) {

					if (listVehicleOccupancyByStationResponse.getVehicleAndOccupancies().length != vehicleIds.size()) {
						throw new IllegalStateException();
					}

					for (VehicleAndOccupancies vehicleOccupancy : listVehicleOccupancyByStationResponse
							.getVehicleAndOccupancies()) {
						Short state = 0; // free
						if (vehicleOccupancy.getOccupancy().length == 1)
							state = 1;
						else
							freeVehicles++;
						DataMapDto typeMap = new DataMapDto();
						String type = "unknown";
						if (forecast == 0)
							type = DataTypeDto.AVAILABILITY;
						else
							type = DataTypeDto.FUTURE_AVAILABILITY;
						// write vehicle state to vehicleData
						List<SimpleRecordDto> dtos = new ArrayList<SimpleRecordDto>();
						dtos.add(new SimpleRecordDto(begin2.getTimeInMillis(), state, 600));
						typeMap.getBranch().put(type,new DataMapDto(dtos));
						vehicleData.getBranch().put(vehicleOccupancy.getVehicle().getId(), typeMap);
					}

					// write available vehicles to stationData
					DataMapDto typeMap = new DataMapDto();
					List<SimpleRecordDto> dtos = new ArrayList<SimpleRecordDto>();
					dtos.add(new SimpleRecordDto(begin2.getTimeInMillis(), freeVehicles, 600));
					typeMap.getBranch().put(DataTypeDto.NUMBER_AVAILABE, new DataMapDto(dtos));
					stationData.getBranch().put(stationId, typeMap);
				}
			}

			// Write data to integreen
			Object pushStations = jsonCarsharingPusher.pushData(INTEGREEN_STATION, stationData);
			if (pushStations instanceof IntegreenException)
				throw new IOException("IntegreenException: real time stations sync");
			Object pushCars = jsonCarsharingPusher.pushData(INTEGREEN_CAR, vehicleData);
			if (pushCars instanceof IntegreenException)
				throw new IOException("IntegreenException: real time cars sync");
		}
		logger.info("REAL TIME DATA ENDED AFTER " + (System.currentTimeMillis() - now));
	}

	private static BoundingBox setUpBoundingBox(double latWS, double lonWS, double latEN, double lonEN) {
		BoundingBox boundingBox = new BoundingBox();
		GeoPos geoPosTypeWS = new GeoPos();
		geoPosTypeWS.setLat(latWS);
		geoPosTypeWS.setLon(lonWS);
		boundingBox.setGeoPosWS(geoPosTypeWS);
		GeoPos geoPosTypeEN = new GeoPos();
		geoPosTypeEN.setLat(latEN);
		geoPosTypeEN.setLon(lonEN);
		boundingBox.setGeoPosEN(geoPosTypeEN);
		return boundingBox;
	}
}