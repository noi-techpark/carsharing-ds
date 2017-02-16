package it.bz.idm.carsharing;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import it.bz.idm.carsharing.dto.GetStationRequestDto;
import it.bz.idm.carsharing.dto.GetVehicleRequestDto;
import it.bz.idm.carsharing.dto.ListStationsByGeoPosRequestDto;
import it.bz.idm.carsharing.dto.ListVehicleOccupancyByStationRequestDto;
import it.bz.idm.carsharing.dto.ListVehiclesByStationRequestDto;
import it.bz.idm.carsharing.dto.NewLisStationsByGeoPosRequest;
import it.bz.idm.carsharing.wsdl.BoundingBox;
import it.bz.idm.carsharing.wsdl.GeoPos;
import it.bz.idm.carsharing.wsdl.ListStationsByGeoPosResponse;
import it.bz.idm.carsharing.wsdl.StationAndVehicles;
import it.bz.idm.carsharing.wsdl.UserAuth;
import it.bz.tis.integreen.util.IntegreenException;
import it.bz.tis.integreen.xmlrpc.FakeConnector;
import it.bz.tis.integreen.xmlrpc.IXMLRPCPusher;

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

	private IXMLRPCPusher xmlrpcPusher;

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
		xmlrpcPusher = new FakeConnector();

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
		boxes.add(setUpBoundingBox(46.86113, 10.375214, 46.459147, 11.059799));
		boxes.add(setUpBoundingBox(46.765265, 11.015081, 46.450277, 11.555557));
		boxes.add(setUpBoundingBox(46.847924, 11.458354, 46.533418, 11.99883));
		boxes.add(setUpBoundingBox(46.455303, 11.166573, 46.218327, 11.521568));
		boxes.add(setUpBoundingBox(47.018653, 11.092758, 46.794448, 11.797256));
		boxes.add(setUpBoundingBox(47.098175, 11.959305, 46.598506, 12.423477));

		jsonParser = new JSONParser();

	}

	public HashMap<String, List<String>> connectForStaticData() throws IOException {
		RestTemplate restTemplate = new RestTemplate();

		List<String> stationIds = new ArrayList<>();
		for (BoundingBox box : boxes) {
			NewLisStationsByGeoPosRequest byGeoPosRequest = new NewLisStationsByGeoPosRequest(userAuth, box);
			HttpEntity<NewLisStationsByGeoPosRequest> entity = new HttpEntity<NewLisStationsByGeoPosRequest>(
					byGeoPosRequest, headers);
			ResponseEntity<String> exchange = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);

			// simlpe GSON
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

			// simple GSON END
			ObjectMapper mapper = new ObjectMapper();
			StringWriter stringWriter = new StringWriter();
			mapper.writeValue(stringWriter, response);

			String responseJackson = stringWriter.getBuffer().toString();
			System.err.println(responseJackson);

			// parse to object

			ListStationsByGeoPosResponse readValue = mapper.readValue(new StringReader(response),
					ListStationsByGeoPosResponse.class);
			System.err.println(readValue);

			// Jackson
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

		// prepare station and vehicle Details
		JSONObject stationDetailsObject = null;
		try {
			stationDetailsObject = (JSONObject) jsonParser.parse(stationDetails);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String stations = null;
		if (stationDetailsObject != null) {
			stations = stationDetailsObject.get("station").toString();
		}

		JSONObject vehicleDetailObject = null;
		try {
			vehicleDetailObject = (JSONObject) jsonParser.parse(vehicleDetailResponse);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String vehicle = null;
		if (vehicleDetailObject != null) {
			vehicle = ((JSONArray) vehicleDetailObject.get("vehicle")).toJSONString();
		}

		Object result = xmlrpcPusher.syncStations(CARSHARINGSTATION_DATASOURCE, stations);
		if (result instanceof IntegreenException) {
			throw new IOException("IntegreenException");
		}
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
		result = xmlrpcPusher.syncStations(CARSHARINGCAR_DATASOURCE, vehicle);
		if (result instanceof IntegreenException) {
			throw new IOException("IntegreenException");
		}

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