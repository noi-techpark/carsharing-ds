package it.bz.idm.carsharing.dto;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import it.bz.idm.carsharing.wsdl.GetStationResponse;

public class MyGetStationResponseDeserializer extends JsonDeserializer<GetStationResponse>{

	@Override
	public GetStationResponse deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		GetStationResponse ret = new GetStationResponse();
		
		return null;
	}

}
