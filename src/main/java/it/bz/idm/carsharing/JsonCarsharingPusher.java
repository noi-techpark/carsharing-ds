package it.bz.idm.carsharing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.bz.idm.json.JSONPusher;

@Component
public class JsonCarsharingPusher extends JSONPusher {
	
	public JsonCarsharingPusher(@Value("${json_url}") String endpoint) {
		super(endpoint);
	}

	@Override
	public String initIntegreenTypology() {
		return "Carsharing";
	}

}
