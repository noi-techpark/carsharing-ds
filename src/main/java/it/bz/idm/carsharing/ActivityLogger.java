package it.bz.idm.carsharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.bz.idm.bdp.dto.SimpleRecordDto;
import it.bz.idm.carsharing.dto.MyListVehicleOccupancyByStationResponse.VehicleAndOccupancies;
import it.bz.idm.carsharing.dto.MyListVehiclesByStationResponse.StationAndVehicles;

@Component
public class ActivityLogger {

	List<StationAndVehicles> stationAndVehicles;
	List<VehicleAndOccupancies> vehicleAndOccupancies;
	HashMap<String, SimpleRecordDto> records;

	public List<StationAndVehicles> getStationAndVehicles() {
		if (stationAndVehicles == null)
			stationAndVehicles = new ArrayList<StationAndVehicles>();
		return stationAndVehicles;
	}

	public List<VehicleAndOccupancies> getVehicleAndOccupancies() {
		if (vehicleAndOccupancies == null)
			vehicleAndOccupancies = new ArrayList<VehicleAndOccupancies>();
		return vehicleAndOccupancies;
	}

	public HashMap<String, SimpleRecordDto> getRecords() {
		if (records == null)
			records = new HashMap<String, SimpleRecordDto>();
		return records;
	}

	@Autowired
	public ActivityLogger() {

	}

}
