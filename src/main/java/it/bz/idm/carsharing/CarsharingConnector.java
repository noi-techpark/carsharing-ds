package it.bz.idm.carsharing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import it.bz.idm.carsharing.dto.CountryDto;
import it.bz.idm.carsharing.dto.ListCountriesRequestDto;
import it.bz.idm.carsharing.dto.ListCountriesResponseDto;
import it.bz.idm.carsharing.wsdl.UserAuth;

/**
 * class for connecting to the carsharing-platform, get the data abd push them
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

	String endpoint;
	String user;
	String password;

	@Autowired
	HAL2ApiClientConfiguration hal2ApiClientConfiguration;

	protected UserAuth userAuthentificationType = null;

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

		userAuthentificationType = new UserAuth();
		userAuthentificationType.setUsername(user);
		userAuthentificationType.setPassword(password);

	}

	public HashMap<String, String[]> connectForStaticData() throws IOException {

		ListCountriesRequestDto listCountriesRequestDto = new ListCountriesRequestDto(userAuthentificationType);

		SimpleHttpClient client = new SimpleHttpClient();
		ListCountriesResponseDto responseDto = (ListCountriesResponseDto) client.get(listCountriesRequestDto, endpoint,
				ListCountriesResponseDto.class);

		System.out.println(responseDto);

		// RestTemplate restTemplate = new RestTemplate();
		//
		// // Create a list for the message converters
		// List<HttpMessageConverter<?>> messageConverters = new
		// ArrayList<HttpMessageConverter<?>>();
		// // Add the Jackson Message converter
		// messageConverters.add(new MappingJackson2HttpMessageConverter());
		// // Add the message converters to the restTemplate
		// restTemplate.setMessageConverters(messageConverters);
		// HttpEntity<ListCountriesRequestDto> entity = new
		// HttpEntity<ListCountriesRequestDto>(listCountriesRequestDto);
		// restTemplate.postForEntity(endpoint, listCountriesRequestDto,
		// Object.class);
		// ListCountriesResponseDto body = postForEntity.getBody();
		// ArrayList<CountryDto> country2 = body.getCountry();
		// if(country2 == null)
		// logger.error("NO COUNTRES");
		// else
		// for(CountryDto countryDto : country2)
		// System.out.println(countryDto.getName());

		// Hal2ApiClient hal2ApiClient =
		// hal2ApiClientConfiguration.hal2ApiClient(hal2ApiClientConfiguration.marshaller());
		// ListCountriesResponseDto countryFromClient =
		// hal2ApiClient.getCountry(endpoint, userAuthentificationType);
		//
		// ArrayList<CountryDto> country2 = countryFromClient.getCountry();

		/**
		 * WORKS
		 */
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.NONE)
				.setVisibility(PropertyAccessor.IS_GETTER, Visibility.PUBLIC_ONLY)
				.setVisibility(PropertyAccessor.GETTER, Visibility.PUBLIC_ONLY)
				.setVisibility(PropertyAccessor.SETTER, Visibility.PUBLIC_ONLY);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		StringWriter sw = new StringWriter();
		mapper.writeValue(sw, listCountriesRequestDto);

		String requestJson = sw.getBuffer().toString();

		logger.debug("callWebService(): jsonRequest:" + requestJson);

		URL url = new URL(this.endpoint);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		OutputStream out = conn.getOutputStream();
		out.write(requestJson.getBytes("UTF-8"));
		out.flush();
		int responseCode = conn.getResponseCode();

		InputStream input = conn.getInputStream();

		ByteArrayOutputStream data = new ByteArrayOutputStream();
		int len;
		byte[] buf = new byte[50000];
		while ((len = input.read(buf)) > 0) {
			data.write(buf, 0, len);
		}
		conn.disconnect();
		String jsonResponse = new String(data.toByteArray(), "UTF-8");
		if (responseCode != 200) {
			throw new IOException(jsonResponse);
		}

		logger.debug("callWebService(): jsonResponse:" + jsonResponse);

		ListCountriesResponseDto response = mapper.readValue(new StringReader(jsonResponse),
				ListCountriesResponseDto.class);

		sw = new StringWriter();
		mapper.writeValue(sw, response);
		logger.debug("callWebService(): parsed response into " + response.getClass().getName() + ":" + sw.toString());

		List<CountryDto> countires = response.getCountry();
		if (countires != null && countires.size() > 0)
			for (CountryDto country : countires)
				System.out.println(country.getName());
		else
			System.err.println("NO COUNTRYS FOUND");

		/**
		 * WORKS
		 */

		// List<BoundingBox> boxes = new ArrayList<BoundingBox>();
		// boxes.add(new BoundingBox(10.375214, 46.459147, 11.059799,
		// 46.86113));
		// boxes.add(new BoundingBox(11.015081, 46.450277, 11.555557,
		// 46.765265));
		// boxes.add(new BoundingBox(11.458354, 46.533418, 11.99883,
		// 46.847924));
		// boxes.add(new BoundingBox(11.166573, 46.218327, 11.521568,
		// 46.455303));
		// boxes.add(new BoundingBox(11.092758, 46.794448, 11.797256,
		// 47.018653));
		// boxes.add(new BoundingBox(11.959305, 46.598506, 12.423477,
		// 47.098175));

		// List<BoundingBox> boxesType = new ArrayList<BoundingBox>();
		// BoundingBox boundingBox = new BoundingBox();
		// GeoPos geoPosTypeWS = new GeoPos();
		// geoPosTypeWS.setLat(46.86113);
		// geoPosTypeWS.setLon(10.375214);
		// boundingBox.setGeoPosWS(geoPosTypeWS);
		//
		// GeoPos geoPosTypeEN = new GeoPos();
		// geoPosTypeEN.setLat(46.459147);
		// geoPosTypeEN.setLon(11.059799);
		// boundingBox.setGeoPosEN(geoPosTypeEN);
		// boxesType.add(boundingBox);
		//
		// ListStationsByGeoPosRequestDto stationsByGeoPosRequest = new
		// ListStationsByGeoPosRequestDto(boundingBox);
		//
		//
		// ObjectMapper mapper = new ObjectMapper();
		// mapper.setVisibility(PropertyAccessor.FIELD, Visibility.NONE)
		// .setVisibility(PropertyAccessor.IS_GETTER, Visibility.PUBLIC_ONLY)
		// .setVisibility(PropertyAccessor.GETTER, Visibility.PUBLIC_ONLY)
		// .setVisibility(PropertyAccessor.SETTER, Visibility.PUBLIC_ONLY);
		// mapper.enable(SerializationFeature.INDENT_OUTPUT);
		//
		// StringWriter sw = new StringWriter();
		// mapper.writeValue(sw, stationsByGeoPosRequest);
		//
		// String requestJson = sw.getBuffer().toString();
		//
		// logger.debug("callWebService(): jsonRequest:" + requestJson);
		//
		// URL url = new URL(this.endpoint);
		// HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		// conn.setRequestMethod("POST");
		// conn.setDoOutput(true);
		// OutputStream out = conn.getOutputStream();
		// out.write(requestJson.getBytes("UTF-8"));
		// out.flush();
		// int responseCode = conn.getResponseCode();
		//
		// InputStream input = conn.getInputStream();
		//
		// ByteArrayOutputStream data = new ByteArrayOutputStream();
		// int len;
		// byte[] buf = new byte[50000];
		// while ((len = input.read(buf)) > 0) {
		// data.write(buf, 0, len);
		// }
		// conn.disconnect();
		// String jsonResponse = new String(data.toByteArray(), "UTF-8");
		// if (responseCode != 200) {
		// throw new IOException(jsonResponse);
		// }
		//
		// logger.debug("callWebService(): jsonResponse:" + jsonResponse);
		//
		// mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
		// false);
		// ListStationsByGeoPosResponseDto response = mapper.readValue(new
		// StringReader(jsonResponse),
		// ListStationsByGeoPosResponseDto.class);
		//
		// mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		// sw = new StringWriter();
		// mapper.writeValue(sw, response);
		// logger.debug("callWebService(): parsed response into " +
		// response.getClass().getName() + ":" + sw.toString());
		//
		// List<Station> stations = response.getStation();
		// if (stations != null && stations.size() > 0)
		// for (Station station : stations)
		// System.out.println(station.getName());
		// else
		// System.err.println("NO STATIONS FOUND");

		//
		// ResponseEntity<ListStationsByGeoPosResponseDto> exchange =
		// restTemplate.exchange(endpoint, HttpMethod.GET,
		// entity, ListStationsByGeoPosResponseDto.class);

		// if (exchange == null)
		// System.out.println("NO");
		// else
		// System.out.println("YES");

		/**
		 * halapiclient
		 */
		// List<BoundingBox> boxesType = new ArrayList<BoundingBox>();
		// BoundingBox boundingBox = new BoundingBox();
		// GeoPos geoPosTypeWS = new GeoPos();
		// geoPosTypeWS.setLat(46.86113);
		// geoPosTypeWS.setLon(10.375214);
		// boundingBox.setGeoPosWS(geoPosTypeWS);
		//
		// GeoPos geoPosTypeEN = new GeoPos();
		// geoPosTypeEN.setLat(46.459147);
		// geoPosTypeEN.setLon(11.059799);
		// boundingBox.setGeoPosEN(geoPosTypeEN);
		// boxesType.add(boundingBox);
		//
		// if(apiClient == null){
		// HAL2ApiClientConfiguration apiClientConfiguration = new
		// HAL2ApiClientConfiguration();
		// apiClient =
		// apiClientConfiguration.hal2ApiClient(apiClientConfiguration.marshaller());
		// }
		//
		// ListStationsByGeoPosResponse stationsByGeoPos =
		// apiClient.getStationsByGeoPos(endpoint, boundingBox,
		// userAuthentificationType);
		//
		// if (stationsByGeoPos == null)
		// logger.error("ERROR in listStationsByGeoPos Request");
		// else
		// logger.info("yeah");

		return null;
	}

	public void connectForRealTimeData(String[] cityUIDs, HashMap<String, String[]> vehicleIdsByStationIds)
			throws IOException {

	}
}
