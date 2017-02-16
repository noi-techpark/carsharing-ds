package it.bz.idm.carsharing.dto;

import java.util.List;

import it.bz.idm.carsharing.wsdl.GetStationRequest;
import it.bz.idm.carsharing.wsdl.UserAuth;

public class MyGetStationRequest {
	String function = "Api.getStation";
	GetStationRequest request;

	public MyGetStationRequest(UserAuth technicalUser, List<Integer> stationIds) {
		request = new GetStationRequest();
		request.setTechnicalUser(technicalUser);
		for (Integer s : stationIds)
			request.getStationUID().add(s);
	}

	public String getFunction() {
		return function;
	}

	public GetStationRequest getRequest() {
		return request;
	}
}
