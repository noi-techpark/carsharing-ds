package it.bz.idm.carsharing;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

/**
 * class to handle errors for the API
 * 
 * @author sdalvai
 *
 */
public class RestTemplateErrorHandler extends DefaultResponseErrorHandler {
	private final Logger logger = LoggerFactory.getLogger(RestTemplateErrorHandler.class);

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		logger.error("RestTemplate Http Error with code: " + response.getRawStatusCode() + " and Status Text: "
				+ response.getStatusText());
	}
}