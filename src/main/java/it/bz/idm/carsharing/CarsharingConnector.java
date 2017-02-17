package it.bz.idm.carsharing;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

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

	public HashMap<Integer, Integer[]> connectForStaticData() throws IOException {
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

		logger.info("Station Details: " + getStationResponse);

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
		ListVehiclesByStationResponse listVehiclesByStationResponse = mapper
				.readValue(new StringReader(vehicleResponse), ListVehiclesByStationResponse.class);

		logger.info("Station Details: " + listVehiclesByStationResponse);

		///////////////////////////////////////////////////////////////
		// Vehicles details
		// ///////////////////////////////////////////////////////////////
		HashMap<Integer, Integer[]> vehicleIdsByStationIds = new HashMap<>();
		List<Integer> vehicleIdsForDetailRequest = new ArrayList<Integer>();
		for (StationAndVehicles stationAndVehicles : listVehiclesByStationResponse.getStationAndVehicles()) {
			// station and vehicles
			Integer[] vehicleIds = new Integer[stationAndVehicles.getVehicle().size()];
			vehicleIdsByStationIds.put(stationAndVehicles.getStation().getUid(), vehicleIds);
			for (int i = 0; i < stationAndVehicles.getVehicle().size(); i++) {
				vehicleIds[i] = stationAndVehicles.getVehicle().get(0).getVehicleUID();
				vehicleIdsForDetailRequest.add(stationAndVehicles.getVehicle().get(0).getVehicleUID());
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

		System.err.println(getVehicleResponse);
		//
		// ///////////////////////////////////////////////////////////////
		// // Write data to integreen
		// ///////////////////////////////////////////////////////////////
		//
		// // prepare station and vehicle Details
		// JSONObject stationDetailsObject = null;
		// try {
		// stationDetailsObject = (JSONObject) jsonParser.parse(stationDetails);
		// } catch (ParseException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// String stations = null;
		// if (stationDetailsObject != null) {
		// stations = stationDetailsObject.get("station").toString();
		// }
		//
		// JSONObject vehicleDetailObject = null;
		// try {
		// vehicleDetailObject = (JSONObject)
		// jsonParser.parse(vehicleDetailResponse);
		// } catch (ParseException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// String vehicle = null;
		// if (vehicleDetailObject != null) {
		// vehicle = ((JSONArray)
		// vehicleDetailObject.get("vehicle")).toJSONString();
		// }
		//
		// Object result =
		// xmlrpcPusher.syncStations(CARSHARINGSTATION_DATASOURCE, stations);
		// if (result instanceof IntegreenException) {
		// throw new IOException("IntegreenException");
		// }
		// //
		// // synchronized (lock)
		// // {
		// // activityLog.report += "syncStations("
		// // + CARSHARINGSTATION_DATASOURCE
		// // + "): "
		// // + responseGetStation.getStation().length
		// // + "\n";
		// // }
		// //
		// result = xmlrpcPusher.syncStations(CARSHARINGCAR_DATASOURCE,
		// vehicle);
		// if (result instanceof IntegreenException) {
		// throw new IOException("IntegreenException");
		// }
		//
		// // synchronized (lock)
		// // {
		// // activityLog.report += "syncStations("
		// // + CARSHARINGCAR_DATASOURCE
		// // + "): "
		// // + responseVehicleDetails.getVehicle().length
		// // + "\n";
		// // }

		return vehicleIdsByStationIds;
	}

	public void connectForRealTimeData(HashMap<Integer, Integer[]> vehicleIdsByStationIds) throws IOException {
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

			begin = begin.normalize();
			end = end.normalize();

			Integer[] stationIds = vehicleIdsByStationIds.keySet().toArray(new Integer[0]);
			for (Integer stationId : stationIds) {
				Integer[] vehicleIds = vehicleIdsByStationIds.get(stationId);
				MyListVehicleOccupancyByStationRequest listVehicleOccupancyByStationRequestDto = new MyListVehicleOccupancyByStationRequest(
						userAuth, begin, end, stationId, vehicleIds);
				HttpEntity<MyListVehicleOccupancyByStationRequest> entity = new HttpEntity<MyListVehicleOccupancyByStationRequest>(
						listVehicleOccupancyByStationRequestDto, headers);

//				logger.info("request started after :" + (System.currentTimeMillis() - now));
				ResponseEntity<String> exchange = restTemplate.exchange(endpoint, HttpMethod.POST, entity,
						String.class);
//				logger.info("request finished after :" + (System.currentTimeMillis() - now));
				String vehicleOccupanciesResponse = exchange.getBody();

				vehicleOccupanciesResponse = vehicleOccupanciesResponse
						.replace("\"showType\":\"vehicle\"", "\"showType\":\"VEHICLE\"")
						.replace("\"occupancyKind\":\"Foreign\"", "\"occupancyKind\":\"FOREIGN\"")
						.replace("\"occupancyKind\":\"Gap\"", "\"occupancyKind\":\"GAP\"");
				;

				ListVehicleOccupancyByStationResponse listVehicleOccupancyByStationResponse = mapper.readValue(
						new StringReader(vehicleOccupanciesResponse), ListVehicleOccupancyByStationResponse.class);
				for (VehicleAndOccupancies vo : listVehicleOccupancyByStationResponse.getVehicleAndOccupancies()) {
					for (Occupancy o : vo.getOccupancy())
						if (forecast == 0){
							logger.info("vehicle: " + vo.getVehicle().getName() + " targa:"
									+ vo.getVehicle().getLicensePlate() + "| occupancy: begin:" + o.getBegin() + " end:"
									+ o.getEnd() + " kind:" + o.getOccupancyKind());
						}
						else
							logger.info("(forecast) vehicle: " + vo.getVehicle().getName() + " targa:"
									+ vo.getVehicle().getLicensePlate() + "| occupancy: begin:" + o.getBegin() + " end:"
									+ o.getEnd() + " kind:" + o.getOccupancyKind());
				}
				System.out.println();
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