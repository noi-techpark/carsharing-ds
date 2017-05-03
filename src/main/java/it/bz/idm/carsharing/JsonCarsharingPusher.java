package it.bz.idm.carsharing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.bz.idm.json.JSONPusher;

@Component
public class JsonCarsharingPusher extends JSONPusher {
	
	@Autowired
	public JsonCarsharingPusher(@Value("${json_push_url}") String urlPush, @Value("${json_sync_url}") String urlSync,
			@Value("${json_datatype_url}") String urlSyncDataTypes) {
		super(urlPush, urlSync, urlSyncDataTypes);
	}

}
