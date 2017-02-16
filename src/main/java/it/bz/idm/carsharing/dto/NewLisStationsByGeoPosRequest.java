package it.bz.idm.carsharing.dto;

import it.bz.idm.carsharing.wsdl.BoundingBox;
import it.bz.idm.carsharing.wsdl.ListStationsByGeoPosRequest;
import it.bz.idm.carsharing.wsdl.UserAuth;

public class NewLisStationsByGeoPosRequest {
	String function = "Api.listStationsByGeoPos";

	ListStationsByGeoPosRequest request;

	public String getFunction() {
		return function;
	}

	public ListStationsByGeoPosRequest getRequest() {
		return request;
	}

	public NewLisStationsByGeoPosRequest(UserAuth techniacalUser, BoundingBox boundingBox) {
		this.request = new ListStationsByGeoPosRequest();
		this.request.setTechnicalUser(techniacalUser);
		this.request.setBoundingBox(boundingBox);
	}
}
