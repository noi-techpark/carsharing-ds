package it.bz.idm.carsharing;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import it.bz.idm.carsharing.dto.GetStationRequestDto;
import it.bz.idm.carsharing.dto.GetVehicleRequestDto;
import it.bz.idm.carsharing.dto.ListStationsByGeoPosRequestDto;
import it.bz.idm.carsharing.dto.ListVehicleOccupancyByStationRequestDto;
import it.bz.idm.carsharing.dto.ListVehiclesByStationRequestDto;
import it.bz.idm.carsharing.wsdl.BoundingBox;
import it.bz.idm.carsharing.wsdl.GeoPos;
import it.bz.idm.carsharing.wsdl.StationAndVehicles;
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
	static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	final static long INTERVALL = 10L * 60L * 1000L;
	public static final String CARSHARINGSTATION_DATASOURCE = "Carsharingstation";
	public static final String CARSHARINGCAR_DATASOURCE = "Carsharingcar";
	private final Logger logger = LoggerFactory.getLogger(CarsharingConnector.class);

	// private CarsharingConnectorConfiguration configuration;

	private String endpoint;
	// @Value("${cred.user}")
	private String user;
	private String password;

	private JSONParser jsonParser;

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

	public CarsharingConnector() {

		Resource resource = new ClassPathResource("application.properties");
		Properties properties = null;
		try {
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.endpoint = properties.getProperty("cred.endpoint");
		this.user = properties.getProperty("cred.user");
		this.password = properties.getProperty("cred.password");

		userAuth = new UserAuth();
		userAuth.setUsername(user);
		userAuth.setPassword(password);

		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		boxes = new ArrayList<BoundingBox>();
		BoundingBox boundingBox = new BoundingBox();
		GeoPos geoPosTypeWS = new GeoPos();
		geoPosTypeWS.setLat(46.86113);
		geoPosTypeWS.setLon(10.375214);
		boundingBox.setGeoPosWS(geoPosTypeWS);
		GeoPos geoPosTypeEN = new GeoPos();
		geoPosTypeEN.setLat(46.459147);
		geoPosTypeEN.setLon(11.059799);
		boundingBox.setGeoPosEN(geoPosTypeEN);

		boxes.add(boundingBox);

		BoundingBox boundingBox2 = new BoundingBox();
		GeoPos geoPosTypeWS2 = new GeoPos();
		geoPosTypeWS2.setLat(46.765265);
		geoPosTypeWS2.setLon(11.015081);
		boundingBox2.setGeoPosWS(geoPosTypeWS2);
		GeoPos geoPosTypeEN2 = new GeoPos();
		geoPosTypeEN2.setLat(46.450277);
		geoPosTypeEN2.setLon(11.555557);
		boundingBox2.setGeoPosEN(geoPosTypeEN2);

		boxes.add(boundingBox2);

		BoundingBox boundingBox3 = new BoundingBox();
		GeoPos geoPosTypeWS3 = new GeoPos();
		geoPosTypeWS3.setLat(46.847924);
		geoPosTypeWS3.setLon(11.458354);
		boundingBox3.setGeoPosWS(geoPosTypeWS3);
		GeoPos geoPosTypeEN3 = new GeoPos();
		geoPosTypeEN3.setLat(46.533418);
		geoPosTypeEN3.setLon(11.99883);
		boundingBox3.setGeoPosEN(geoPosTypeEN3);

		boxes.add(boundingBox3);

		BoundingBox boundingBox4 = new BoundingBox();
		GeoPos geoPosTypeWS4 = new GeoPos();
		geoPosTypeWS4.setLat(46.455303);
		geoPosTypeWS4.setLon(11.166573);
		boundingBox4.setGeoPosWS(geoPosTypeWS4);
		GeoPos geoPosTypeEN4 = new GeoPos();
		geoPosTypeEN4.setLat(46.218327);
		geoPosTypeEN4.setLon(11.521568);
		boundingBox4.setGeoPosEN(geoPosTypeEN4);

		boxes.add(boundingBox4);
		BoundingBox boundingBox5 = new BoundingBox();
		GeoPos geoPosTypeWS5 = new GeoPos();
		geoPosTypeWS5.setLat(47.018653);
		geoPosTypeWS5.setLon(11.092758);
		boundingBox5.setGeoPosWS(geoPosTypeWS5);
		GeoPos geoPosTypeEN5 = new GeoPos();
		geoPosTypeEN5.setLat(46.794448);
		geoPosTypeEN5.setLon(11.797256);
		boundingBox5.setGeoPosEN(geoPosTypeEN5);

		boxes.add(boundingBox5);

		BoundingBox boundingBox6 = new BoundingBox();
		GeoPos geoPosTypeWS6 = new GeoPos();
		geoPosTypeWS6.setLat(47.098175);
		geoPosTypeWS6.setLon(11.959305);
		boundingBox6.setGeoPosWS(geoPosTypeWS6);
		GeoPos geoPosTypeEN6 = new GeoPos();
		geoPosTypeEN6.setLat(46.598506);
		geoPosTypeEN6.setLon(12.423477);
		boundingBox6.setGeoPosEN(geoPosTypeEN6);

		boxes.add(boundingBox6);

		jsonParser = new JSONParser();

	}

	public HashMap<String, List<String>> connectForStaticData() {
		RestTemplate restTemplate = new RestTemplate();

		List<String> stationIds = new ArrayList<>();
		for (BoundingBox box : boxes) {
			ListStationsByGeoPosRequestDto byGeoPosRequest = new ListStationsByGeoPosRequestDto(box, userAuth);
			HttpEntity<ListStationsByGeoPosRequestDto> entity = new HttpEntity<ListStationsByGeoPosRequestDto>(
					byGeoPosRequest, headers);
			ResponseEntity<String> exchange = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
			String response = exchange.getBody();
			JSONObject stations = null;
			try {
				Object parse = jsonParser.parse(response);
				if (parse instanceof JSONObject)
					stations = (JSONObject) parse;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (stations != null && stations.get("station") != null) {
				JSONArray stationArray = (JSONArray) stations.get("station");
				for (Object station : stationArray) {
					JSONObject stationJsonObject = (JSONObject) station;
					stationIds.add(stationJsonObject.get("uid").toString());
				}
			}

		}

		// getStation details
		GetStationRequestDto getStationRequest = new GetStationRequestDto(userAuth, stationIds);

		HttpEntity<GetStationRequestDto> getStationEntity = new HttpEntity<GetStationRequestDto>(getStationRequest,
				headers);
		ResponseEntity<String> exchange = restTemplate.exchange(endpoint, HttpMethod.POST, getStationEntity,
				String.class);
		String stationDetails = exchange.getBody();
		// logger.info("Station Details: " + stationDetails);

		//////////////////////////////////////////////////////////////
		// Vehicles by stations
		///////////////////////////////////////////////////////////////

		ListVehiclesByStationRequestDto vehicles = new ListVehiclesByStationRequestDto(userAuth, stationIds);
		HttpEntity<ListVehiclesByStationRequestDto> vehicleListRequest = new HttpEntity<ListVehiclesByStationRequestDto>(
				vehicles, headers);

		ResponseEntity<String> exchangeVehicle = restTemplate.exchange(endpoint, HttpMethod.POST, vehicleListRequest,
				String.class);
		String vehicleResponse = exchangeVehicle.getBody();

		// prepare for grtting vehicle details
		List<StationAndVehicles> stationAndVehicles = new ArrayList<>();

		JSONObject stationAndVehiclesJson = null;
		try {
			stationAndVehiclesJson = (JSONObject) jsonParser.parse(vehicleResponse);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// logger.info("Station and Vehicles: " + stationAndVehiclesJson);

		///////////////////////////////////////////////////////////////
		// Vehicles details
		///////////////////////////////////////////////////////////////
		HashMap<String, List<String>> vehicleIdsByStationIds = new HashMap<>();
		List<String> vehicleIdsForDetailRequest = new ArrayList<String>();
		JSONArray stationArray = null;
		if (stationAndVehiclesJson != null) {
			stationArray = (JSONArray) stationAndVehiclesJson.get("stationAndVehicles");
			for (Object stationAndVehiclesObject : stationArray) {
				// station and vehicles
				JSONObject stationAndVehiclesJsonObject = (JSONObject) stationAndVehiclesObject;
				JSONArray vehiclesJsonArray = (JSONArray) stationAndVehiclesJsonObject.get("vehicle");
				JSONObject stationJsonObject = (JSONObject) stationAndVehiclesJsonObject.get("station");
				List<String> vIds = new ArrayList<>();
				vehicleIdsByStationIds.put(stationJsonObject.get("uid").toString(), vIds);
				for (Object singleVehicle : vehiclesJsonArray) {
					JSONObject singleVehicleJSONObject = (JSONObject) singleVehicle;
					String uid = singleVehicleJSONObject.get("vehicleUID").toString();
					vIds.add(uid);
					vehicleIdsForDetailRequest.add(uid);
				}
			}
		}

		// getDetails

		GetVehicleRequestDto getVehicleRequestDto = new GetVehicleRequestDto(userAuth, vehicleIdsForDetailRequest);

		HttpEntity<GetVehicleRequestDto> entityVehicleDetail = new HttpEntity<GetVehicleRequestDto>(
				getVehicleRequestDto, headers);
		ResponseEntity<String> exchangeVehicleDetail = restTemplate.exchange(endpoint, HttpMethod.POST,
				entityVehicleDetail, String.class);
		String vehicleDetailResponse = exchangeVehicleDetail.getBody();

		///////////////////////////////////////////////////////////////
		// Write data to integreen
		///////////////////////////////////////////////////////////////

		return vehicleIdsByStationIds;
	}

	public void connectForRealTimeData(HashMap<String, List<String>> vehicleIdsByStationIds) throws IOException {
		RestTemplate restTemplate = new RestTemplate();

		Long now = System.currentTimeMillis();
		logger.info("REAL TIME DATA STARTED AT " + now);
		for (long forecast : new long[] { 0, 30L * 60L * 1000L }) {
			int i = 0;
			String[] stationIds = vehicleIdsByStationIds.keySet().toArray(new String[0]);
			String begin = SIMPLE_DATE_FORMAT.format(new Date(now + forecast));
			String end = SIMPLE_DATE_FORMAT.format(new Date(now + forecast + INTERVALL));
			for (String stationId : stationIds) {
				List<String> vehicleIds = vehicleIdsByStationIds.get(stationId);
				ListVehicleOccupancyByStationRequestDto listVehicleOccupancyByStationRequestDto = new ListVehicleOccupancyByStationRequestDto(
						userAuth, begin, end, stationId, vehicleIds);
				HttpEntity<ListVehicleOccupancyByStationRequestDto> entity = new HttpEntity<ListVehicleOccupancyByStationRequestDto>(
						listVehicleOccupancyByStationRequestDto, headers);

				logger.info("request started after :" + (System.currentTimeMillis() - now));
				ResponseEntity<String> exchange = restTemplate.exchange(endpoint, HttpMethod.POST, entity,
						String.class);
				logger.info("request finished after :" + (System.currentTimeMillis() - now));
				String vehicleOccupanciesResponse = exchange.getBody();
				JSONObject vehicleOccupancyObject = null;

				logger.info("parsing started after :" + (System.currentTimeMillis() - now));
				try {
					vehicleOccupancyObject = (JSONObject) jsonParser.parse(vehicleOccupanciesResponse);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				for (Object o : (JSONArray) vehicleOccupancyObject.get("vehicleAndOccupancies")) {
					JSONObject jo = (JSONObject) o;
					if (forecast == 0 && jo.get("occupancy").toString().length() > 2)
						logger.info(i + " Vehicle: " + ((JSONObject) jo.get("vehicle")).get("name") + " Occupancy: "
								+ jo.get("occupancy"));
					else if (jo.get("occupancy").toString().length() > 2)
						logger.info(i + "F --- Vehicle: " + ((JSONObject) jo.get("vehicle")).get("name")
								+ " Occupancy: " + jo.get("occupancy"));
					i++;
					logger.info("parsing partially ended after :" + (System.currentTimeMillis() - now));
				}
			}
		}
		logger.info("REAL TIME DATA ENDED AFTER " + (System.currentTimeMillis() - now));

		///////////////////////////////////////////////////////////////
		// Write data to integreen
		///////////////////////////////////////////////////////////////
		
		
	}
}