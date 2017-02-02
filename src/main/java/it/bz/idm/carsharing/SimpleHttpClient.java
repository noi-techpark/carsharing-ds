package it.bz.idm.carsharing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class SimpleHttpClient {
	
	private final Logger logger = LoggerFactory.getLogger(SimpleHttpClient.class);
	
	
	public SimpleHttpClient(){
		
	}
	
	public Object get(Object request,String endpoint,Class classType)  throws IOException{
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.NONE)
				.setVisibility(PropertyAccessor.IS_GETTER, Visibility.PUBLIC_ONLY)
				.setVisibility(PropertyAccessor.GETTER, Visibility.PUBLIC_ONLY)
				.setVisibility(PropertyAccessor.SETTER, Visibility.PUBLIC_ONLY);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		StringWriter sw = new StringWriter();
		mapper.writeValue(sw, request);

		String requestJson = sw.getBuffer().toString();

		logger.debug("callWebService(): jsonRequest:" + requestJson);

		URL url = new URL(endpoint);
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

		Object response = mapper.readValue(new StringReader(jsonResponse),
				classType);

//		sw = new StringWriter();
//		mapper.writeValue(sw, response);
//		logger.debug("callWebService(): parsed response into " + response.getClass().getName() + ":" + sw.toString());
//
//		List<CountryDto> countires = response.getCountry();
//		if (countires != null && countires.size() > 0)
//			for (CountryDto country : countires)
//				System.out.println(country.getName());
//		else
//			System.err.println("NO COUNTRYS FOUND");
		
		return response;
	}
}
