package it.bz.idm.carsharing.dto;

import javax.xml.datatype.XMLGregorianCalendar;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import it.bz.idm.carsharing.wsdl.ListVehicleOccupancyByStationRequest;
import it.bz.idm.carsharing.wsdl.UserAuth;

@JsonSerialize(using = MyListVehicleOccupancyByStationRequestSerializer.class)
public class MyListVehicleOccupancyByStationRequest {
	String function;
	ListVehicleOccupancyByStationRequest request;

	public MyListVehicleOccupancyByStationRequest(UserAuth technicalUser, XMLGregorianCalendar begin,
			XMLGregorianCalendar end, String stationUID, String[] vehicleUids) {
		this.request = new ListVehicleOccupancyByStationRequest();
		this.request.setTechnicalUser(technicalUser);
		this.request.setBegin(begin);
		this.request.setEnd(end);
		this.request.setStationUID(Integer.parseInt(stationUID));
		for (String vehicleUid : vehicleUids)
			this.request.getVehicleUID().add(Integer.parseInt(vehicleUid));
		this.function = "Api.listVehicleOccupancyByStation";
	}
	
	public MyListVehicleOccupancyByStationRequest(){
	}

	public String getFunction() {
		return function;
	}

	public ListVehicleOccupancyByStationRequest getRequest() {
		return request;
	}
}
