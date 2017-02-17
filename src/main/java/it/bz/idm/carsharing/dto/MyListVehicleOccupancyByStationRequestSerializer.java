package it.bz.idm.carsharing.dto;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class MyListVehicleOccupancyByStationRequestSerializer
		extends JsonSerializer<MyListVehicleOccupancyByStationRequest> {

	@Override
	public void serialize(MyListVehicleOccupancyByStationRequest value, JsonGenerator gen,
			SerializerProvider serializers) throws IOException, JsonProcessingException {
		// TODO Auto-generated method stub
		int[] vehicleUids = new int[value.getRequest().getVehicleUID().size()];
		for(int i = 0; i < value.getRequest().getVehicleUID().size() ; i++){
			vehicleUids[i] = value.getRequest().getVehicleUID().get(i);
		}
		
		
		gen.writeStartObject();
		gen.writeObjectFieldStart("request");
		gen.writeObjectField("technicalUser", value.getRequest().getTechnicalUser());
		gen.writeArrayFieldStart("vehicleUID");
		for(int i : vehicleUids)
			gen.writeNumber(i);
		gen.writeEndArray();
		gen.writeStringField("stationUID", Integer.toString(value.getRequest().getStationUID()));
		gen.writeStringField("begin", value.getRequest().getBegin().toString());
		gen.writeStringField("end", value.getRequest().getEnd().toString());
		gen.writeEndObject();
		gen.writeStringField("function", value.getFunction());
		gen.writeEndObject();
	}

}
