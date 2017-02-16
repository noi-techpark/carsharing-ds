package it.bz.idm.carsharing.dto;

import java.util.List;

import it.bz.idm.carsharing.wsdl.GetVehicleRequest;
import it.bz.idm.carsharing.wsdl.UserAuth;

public class MyGetVehicleRequest {
	GetVehicleRequest request;
	String function;

	public MyGetVehicleRequest(UserAuth technicalUser, List<Integer> vehicleIds) {
		this.request = new GetVehicleRequest();
		this.request.setTechnicalUser(technicalUser);
		for (Integer vehicleId : vehicleIds)
			this.request.getVehicleUID().add(vehicleId);
		this.function = "Api.getVehicle";
	}

	public GetVehicleRequest getRequest() {
		return request;
	}

	public String getFunction() {
		return function;
	}
}
