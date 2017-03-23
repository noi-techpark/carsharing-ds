package it.bz.idm.carsharing;

import org.springframework.stereotype.Component;

import it.bz.idm.bdp.xmlrpc.XMLRPCPusher;

@Component
public class CarsharingStationSync extends XMLRPCPusher {

	@Override
	public String initIntegreenTypology() {
		return "Carsharingstation";
	}

}
