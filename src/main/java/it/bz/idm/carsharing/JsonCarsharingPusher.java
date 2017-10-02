package it.bz.idm.carsharing;

import org.springframework.stereotype.Component;

import it.bz.idm.bdp.json.JSONPusher;


@Component
public class JsonCarsharingPusher extends JSONPusher {
	
	@Override
	public String initIntegreenTypology() {
		return "Carsharing";
	}

}
