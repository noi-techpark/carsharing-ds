package it.bz.tis.envds;


import it.bz.tis.integreen.dto.Remark;
import it.bz.tis.integreen.xmlrpc.XMLRPCPusher;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;

public class EnvironmentPusher extends XMLRPCPusher {

	
	private static final String DATASET_TYPE = "Environmentstation";

	public EnvironmentPusher() {
	}

	public void pushHourlyData(List<Remark> hourlyRemarks) throws XmlRpcException {
		pushData(DATASET_TYPE, new Object[]{hourlyRemarks});
	}
}
