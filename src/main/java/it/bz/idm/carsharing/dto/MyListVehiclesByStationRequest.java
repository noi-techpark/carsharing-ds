package it.bz.idm.carsharing.dto;

import java.util.List;

import it.bz.idm.carsharing.wsdl.ListVehiclesByStationRequest;
import it.bz.idm.carsharing.wsdl.UserAuth;

public class MyListVehiclesByStationRequest {
	String function = "Api.listVehiclesByStation";
	ListVehiclesByStationRequest request;

	public MyListVehiclesByStationRequest(UserAuth technicalUser, List<Integer> stationIds) {
		this.request = new ListVehiclesByStationRequest();
		this.request.setTechnicalUser(technicalUser);
		for (Integer stationId : stationIds)
			this.request.getStationUID().add(stationId);
	}

	public String getFunction() {
		return function;
	}

	public ListVehiclesByStationRequest getRequest() {
		return request;
	}

}
