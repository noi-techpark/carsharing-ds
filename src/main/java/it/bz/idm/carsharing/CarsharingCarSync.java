package it.bz.idm.carsharing;

import org.springframework.stereotype.Component;

import it.bz.idm.bdp.dto.DataMapDto;
import it.bz.idm.bdp.xmlrpc.XMLRPCPusher;

@Component
public class CarsharingCarSync extends XMLRPCPusher{
	
	@Override
	public String initIntegreenTypology() {
		return "Carsharingcar";
	}

	@Override
	public Object pushData(String datasourceName, DataMapDto dto) {
		// TODO Auto-generated method stub
		return null;
	}

}
