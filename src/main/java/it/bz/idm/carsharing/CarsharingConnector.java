package it.bz.idm.carsharing;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.bz.idm.carsharing.dto.MyGetStationRequest;
import it.bz.idm.carsharing.dto.MyGetVehicleRequest;
import it.bz.idm.carsharing.dto.MyListStationsByGeoPosRequest;
import it.bz.idm.carsharing.dto.MyListVehicleOccupancyByStationRequest;
import it.bz.idm.carsharing.dto.MyListVehiclesByStationRequest;
import it.bz.idm.carsharing.wsdl.BoundingBox;
import it.bz.idm.carsharing.wsdl.GeoPos;
import it.bz.idm.carsharing.wsdl.GetStationResponse;
import it.bz.idm.carsharing.wsdl.GetVehicleResponse;
import it.bz.idm.carsharing.wsdl.ListStationsByGeoPosResponse;
import it.bz.idm.carsharing.wsdl.ListVehicleOccupancyByStationResponse;
import it.bz.idm.carsharing.wsdl.ListVehicleOccupancyByStationResponse.VehicleAndOccupancies;
import it.bz.idm.carsharing.wsdl.ListVehiclesByStationResponse;
import it.bz.idm.carsharing.wsdl.Occupancy;
import it.bz.idm.carsharing.wsdl.Station;
import it.bz.idm.carsharing.wsdl.StationAndVehicles;
import it.bz.idm.carsharing.wsdl.UserAuth;
import it.bz.idm.carsharing.wsdl.Vehicle;
import it.bz.tis.integreen.carsharingbzit.tis.CarSharingXMLRPCPusher;
import it.bz.tis.integreen.carsharingbzit.tis.IXMLRPCPusher;
import it.bz.tis.integreen.dto.DataTypeDto;
import it.bz.tis.integreen.dto.SimpleRecordDto;
import it.bz.tis.integreen.dto.TypeMapDto;
import it.bz.tis.integreen.util.IntegreenException;

/**
 * class for connecting to the carsharing-platform, get the data and push them
 * to the inegreenPlatform
 * 
 * @author sdalvai
 *
 */

@Component
public class CarsharingConnector {
	// static final SimpleDateFormat SIMPLE_DATE_FORMAT = new
	// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	final static long INTERVALL = 10L * 60L * 1000L;
	public static final String CARSHARINGSTATION_DATASOURCE = "Carsharingstation";
	public static final String CARSHARINGCAR_DATASOURCE = "Carsharingcar";
	private final Logger logger = LoggerFactory.getLogger(CarsharingConnector.class);

	// private CarsharingConnectorConfiguration configuration;
	@Autowired
	private ActivityLogger activityLogger;

	private IXMLRPCPusher xmlrpcPusher;

	private String endpoint;
	// @Value("${cred.user}")
	private String user;
	private String password;

	private HttpHeaders headers;

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

		xmlrpcPusher = new CarSharingXMLRPCPusher();

		userAuth = new UserAuth();
		userAuth.setUsername(user);
		userAuth.setPassword(password);

		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		boxes = new ArrayList<BoundingBox>();
		boxes.add(setUpBoundingBox(46.86113, 10.375214, 46.459147, 11.059799));
		boxes.add(setUpBoundingBox(46.765265, 11.015081, 46.450277, 11.555557));
		boxes.add(setUpBoundingBox(46.847924, 11.458354, 46.533418, 11.99883));
		boxes.add(setUpBoundingBox(46.455303, 11.166573, 46.218327, 11.521568));
		boxes.add(setUpBoundingBox(47.018653, 11.092758, 46.794448, 11.797256));
		boxes.add(setUpBoundingBox(47.098175, 11.959305, 46.598506, 12.423477));

		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
		mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	}

	public HashMap<Integer, List<Integer>> connectForStaticData(HashMap<String, List<Integer>> vehicleIdsByStationNames)
			throws IOException {
		Long now = System.currentTimeMillis();
		logger.info("STATIC DATA STARTED AT " + now);

		RestTemplate restTemplate = new RestTemplate();
		List<Integer> stationIds = new ArrayList<>();
		for (BoundingBox box : boxes) {
			MyListStationsByGeoPosRequest byGeoPosRequest = new MyListStationsByGeoPosRequest(userAuth, box);
			HttpEntity<MyListStationsByGeoPosRequest> entity = new HttpEntity<MyListStationsByGeoPosRequest>(
					byGeoPosRequest, headers);
			ResponseEntity<String> exchange = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);

			String response = exchange.getBody();

			// parse to object
			// check if station array is empty
			ListStationsByGeoPosResponse stations = mapper.readValue(new StringReader(response),
					ListStationsByGeoPosResponse.class);
			if (stations != null)
				for (Station station : stations.getStation()) {
					stationIds.add(station.getUid());
				}
			// Jackson
		}

		///////////////////////////////////////////////////////////////
		// Stations details
		///////////////////////////////////////////////////////////////
		MyGetStationRequest getStationRequest = new MyGetStationRequest(userAuth, stationIds);

		HttpEntity<MyGetStationRequest> getStationEntity = new HttpEntity<MyGetStationRequest>(getStationRequest,
				headers);
		ResponseEntity<String> exchange = restTemplate.exchange(endpoint, HttpMethod.POST, getStationEntity,
				String.class);
		String stationDetails = exchange.getBody();

		// remove 'km' distance of NeighborStation to prevent
		// InvalidFormatException-> String to Float
		stationDetails = stationDetails.replace(" km", "");

		GetStationResponse getStationResponse = mapper.readValue(new StringReader(stationDetails),
				GetStationResponse.class);
		logger.info("ALL STATIONS");
		for (Station s : getStationResponse.getStation())
			logger.info(s.getName());

		//////////////////////////////////////////////////////////////
		// Vehicles by stations
		///////////////////////////////////////////////////////////////

		MyListVehiclesByStationRequest vehicles = new MyListVehiclesByStationRequest(userAuth, stationIds);
		HttpEntity<MyListVehiclesByStationRequest> vehicleListRequest = new HttpEntity<MyListVehiclesByStationRequest>(
				vehicles, headers);

		ResponseEntity<String> exchangeVehicle = restTemplate.exchange(endpoint, HttpMethod.POST, vehicleListRequest,
				String.class);
		String vehicleResponse = exchangeVehicle.getBody();

		// resplace vehicle with VEHICLE to prevent enum not found exception
		vehicleResponse = vehicleResponse.replace("\"showType\":\"vehicle\"", "\"showType\":\"VEHICLE\"");

		// JUST FOR LOGGING AND PREPARING BEHICLEUUIDS BY STATION NAMES
		ListVehiclesByStationResponse listVehiclesByStationResponse = mapper
				.readValue(new StringReader(vehicleResponse), ListVehiclesByStationResponse.class);
		logger.info("STATIONS AND VEHICLES");
		for (StationAndVehicles sv : listVehiclesByStationResponse.getStationAndVehicles()) {
			logger.info("station " + sv.getStation().getName());
			logger.info("VEHICLES");
			List<Integer> vIds = new ArrayList<>();
			for (Vehicle v : sv.getVehicle()) {
				logger.info("vehicle " + v.getName() + " targa: " + v.getLicensePlate());
				vIds.add(v.getVehicleUID());
			}
			vehicleIdsByStationNames.put(sv.getStation().getName(), vIds);

		}

		///////////////////////////////////////////////////////////////
		// Vehicles details
		// ///////////////////////////////////////////////////////////////
		activityLogger.getStationAndVehicles().clear();
		HashMap<Integer, List<Integer>> vehicleIdsByStationIds = new HashMap<>();
		List<Integer> vehicleIdsForDetailRequest = new ArrayList<Integer>();
		for (StationAndVehicles stationAndVehicles : listVehiclesByStationResponse.getStationAndVehicles()) {
			// station and vehicles
			activityLogger.getStationAndVehicles().add(stationAndVehicles);
			List<Integer> vehicleIds = new ArrayList<Integer>();
			vehicleIdsByStationIds.put(stationAndVehicles.getStation().getUid(), vehicleIds);
			for (int i = 0; i < stationAndVehicles.getVehicle().size(); i++) {
				vehicleIds.add(stationAndVehicles.getVehicle().get(i).getVehicleUID());
				vehicleIdsForDetailRequest.add(stationAndVehicles.getVehicle().get(i).getVehicleUID());
			}
		}
		//
		// // getDetails
		//
		MyGetVehicleRequest getVehicleRequestDto = new MyGetVehicleRequest(userAuth, vehicleIdsForDetailRequest);

		HttpEntity<MyGetVehicleRequest> entityVehicleDetail = new HttpEntity<MyGetVehicleRequest>(getVehicleRequestDto,
				headers);
		ResponseEntity<String> exchangeVehicleDetail = restTemplate.exchange(endpoint, HttpMethod.POST,
				entityVehicleDetail, String.class);
		String vehicleDetailResponse = exchangeVehicleDetail.getBody();

		vehicleDetailResponse = vehicleDetailResponse.replace("\"showType\":\"vehicle\"", "\"showType\":\"VEHICLE\"");

		GetVehicleResponse getVehicleResponse = mapper.readValue(new StringReader(vehicleDetailResponse),
				GetVehicleResponse.class);

		///////////////////////////////////////////////////////////////
		// Write data to integreen
		///////////////////////////////////////////////////////////////

		Object result = xmlrpcPusher.syncStations(CARSHARINGSTATION_DATASOURCE,
				getStationResponse.getStation().toArray());
		if (result instanceof IntegreenException) {
			throw new IOException("IntegreenException");
		}

		result = xmlrpcPusher.syncStations(CARSHARINGCAR_DATASOURCE, getVehicleResponse.getVehicle().toArray());
		if (result instanceof IntegreenException) {
			throw new IOException("IntegreenException");
		}

		logger.info("STATIC DATA ENDED AFTER " + (System.currentTimeMillis() - now));
		return vehicleIdsByStationIds;
	}

	public void connectForRealTimeData(HashMap<Integer, List<Integer>> vehicleIdsByStationIds,
			HashMap<String, List<Integer>> vehicleIdsByStationNames) throws IOException {
		RestTemplate restTemplate = new RestTemplate();

		// XML
		HttpHeaders headersXML = new HttpHeaders();
		headersXML.setContentType(MediaType.APPLICATION_ATOM_XML);

		Long now = System.currentTimeMillis();
		logger.info("REAL TIME DATA STARTED AT " + now);
		for (long forecast : new long[] { 0, 30L * 60L * 1000L }) {
			GregorianCalendar begin2 = new GregorianCalendar();
			begin2.setTime(new Date(now + forecast));
			GregorianCalendar end2 = new GregorianCalendar();
			end2.setTime(new Date(now + forecast + INTERVALL));
			XMLGregorianCalendar begin = null;
			XMLGregorianCalendar end = null;
			try {
				begin = DatatypeFactory.newInstance().newXMLGregorianCalendar(begin2);
				end = DatatypeFactory.newInstance().newXMLGregorianCalendar(end2);
			} catch (DatatypeConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Integer[] stationIds = new Integer[vehicleIdsByStationIds.keySet().size()];
			int i = 0;
			for (Integer id : vehicleIdsByStationIds.keySet()) {
				stationIds[i] = id;
				i++;
			}
			Arrays.sort(stationIds);

			HashMap<Integer, TypeMapDto> stationData = new HashMap<Integer, TypeMapDto>();
			HashMap<Integer, TypeMapDto> vehicleData = new HashMap<Integer, TypeMapDto>();
			for (Integer stationId : stationIds) {
				List<Integer> vehicleIds = vehicleIdsByStationIds.get(stationId);
				MyListVehicleOccupancyByStationRequest listVehicleOccupancyByStationRequestDto = new MyListVehicleOccupancyByStationRequest(
						userAuth, begin, end, stationId, vehicleIds.toArray(new Integer[0]));
				HttpEntity<MyListVehicleOccupancyByStationRequest> entity = new HttpEntity<MyListVehicleOccupancyByStationRequest>(
						listVehicleOccupancyByStationRequestDto, headers);

				// logger.info("request started after :" +
				// (System.currentTimeMillis() - now));
				ResponseEntity<String> exchange = restTemplate.exchange(endpoint, HttpMethod.POST, entity,
						String.class);
				// logger.info("request finished after :" +
				// (System.currentTimeMillis() - now));
				String vehicleOccupanciesResponse = exchange.getBody();

				vehicleOccupanciesResponse = vehicleOccupanciesResponse
						.replace("\"showType\":\"vehicle\"", "\"showType\":\"VEHICLE\"")
						.replace("\"occupancyKind\":\"Foreign\"", "\"occupancyKind\":\"FOREIGN\"")
						.replace("\"occupancyKind\":\"Gap\"", "\"occupancyKind\":\"GAP\"");
				;

				ListVehicleOccupancyByStationResponse listVehicleOccupancyByStationResponse = mapper.readValue(
						new StringReader(vehicleOccupanciesResponse), ListVehicleOccupancyByStationResponse.class);
				activityLogger.getVehicleAndOccupancies().clear();
				// Same number of responses as the number to requests
				if (listVehicleOccupancyByStationResponse.getVehicleAndOccupancies().size() != vehicleIds.size()) {
					throw new IllegalStateException();
				}
				int free = 0;
				for (VehicleAndOccupancies vehicleOccupancy : listVehicleOccupancyByStationResponse
						.getVehicleAndOccupancies()) {
					if (forecast == 0)
						activityLogger.getVehicleAndOccupancies().add(vehicleOccupancy);

					if (vehicleOccupancy.getOccupancy().size() > 1) {
						throw new IllegalStateException("Why???");
					}
					int state = 0; // free
					if (vehicleOccupancy.getOccupancy().size() == 1) {
						state = 1;
					} else {
						free++;
					}
					TypeMapDto typeMap = new TypeMapDto();
					vehicleData.put(vehicleOccupancy.getVehicle().getVehicleUID(), typeMap);
					String type = "unknown";
					if (forecast == 0)
						type = DataTypeDto.AVAILABILITY;
					else
						type = DataTypeDto.FUTURE_AVAILABILITY;
					Set<SimpleRecordDto> dtos = typeMap.getRecordsByType().get(type);
					if (dtos == null) {
						dtos = new HashSet<SimpleRecordDto>();
						typeMap.getRecordsByType().put(type, dtos);
					}
					dtos.add(new SimpleRecordDto(begin2.getTimeInMillis() + forecast, state + 0., 600));

					// for logging

					// if (vo.getOccupancy() != null && vo.getOccupancy().size()
					// > 0)
					// for (Occupancy o : vo.getOccupancy()) {
					// if (forecast == 0) {
					// for (String key : vehicleIdsByStationNames.keySet()) {
					// if
					// (vehicleIdsByStationNames.get(key).contains(vo.getVehicle().getVehicleUID()))
					// logger.info("s: " + key + " vehicle: " +
					// vo.getVehicle().getName() + " targa:"
					// + vo.getVehicle().getLicensePlate() + " | occupancy:
					// begin:"
					// + o.getBegin() + " end:" + o.getEnd() + " kind:"
					// + o.getOccupancyKind());
					// }
					// } else {
					// for (String key : vehicleIdsByStationNames.keySet()) {
					// if
					// (vehicleIdsByStationNames.get(key).contains(vo.getVehicle().getVehicleUID()))
					// logger.info("(forecast) s: " + key + " vehicle: " +
					// vo.getVehicle().getName()
					// + " targa:" + vo.getVehicle().getLicensePlate() + " |
					// occupancy: begin:"
					// + o.getBegin() + " end:" + o.getEnd() + " kind:"
					// + o.getOccupancyKind());
					// }
					// }

					// }
					// else {
					// for (String key : vehicleIdsByStationNames.keySet()) {
					// if
					// (vehicleIdsByStationNames.get(key).contains(vo.getVehicle().getVehicleUID()))
					// logger.info("FREE s: " + key + " vehicle: " +
					// vo.getVehicle().getName() + " targa:"
					// + vo.getVehicle().getLicensePlate());
					// }
					// }
				}
				Set<SimpleRecordDto> dtos = new HashSet<SimpleRecordDto>();
				TypeMapDto typeMap = new TypeMapDto();
				typeMap.getRecordsByType().put(DataTypeDto.NUMBER_AVAILABE, dtos);
				if (forecast == 0)
					dtos.add(new SimpleRecordDto(begin2.getTimeInMillis() + forecast, free + 0., 600));
				stationData.put(stationId, typeMap);
			}
			///////////////////////////////////////////////////////////////
			// Write data to integreen
			///////////////////////////////////////////////////////////////

			Object result = xmlrpcPusher.pushData(CARSHARINGSTATION_DATASOURCE, new Object[] { stationData });
			if (result instanceof IntegreenException) {
				throw new IOException("IntegreenException");
			}
			result = xmlrpcPusher.pushData(CARSHARINGCAR_DATASOURCE, new Object[] { vehicleData });
			if (result instanceof IntegreenException) {
				throw new IOException("IntegreenException");
			}
			logger.info("REAL TIME DATA ENDED AFTER " + (System.currentTimeMillis() - now));
		}

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