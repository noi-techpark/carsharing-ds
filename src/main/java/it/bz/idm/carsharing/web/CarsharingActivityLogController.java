package it.bz.idm.carsharing.web;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import it.bz.idm.carsharing.ActivityLogger;
import it.bz.idm.carsharing.dto.ChatBotMessageDto;
import it.bz.idm.carsharing.dto.ChatBotTextDto;
import it.bz.idm.carsharing.wsdl.ListVehicleOccupancyByStationResponse.VehicleAndOccupancies;
import it.bz.idm.carsharing.wsdl.StationAndVehicles;

@RestController
@RequestMapping("/carsharing/")
public class CarsharingActivityLogController {
	@Autowired
	private ActivityLogger activityLogger;

	@RequestMapping(value = "stationAndVehicles", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<List<StationAndVehicles>> stationAndVehicles() {
		ResponseEntity<List<StationAndVehicles>> entity = new ResponseEntity<List<StationAndVehicles>>(
				activityLogger.getStationAndVehicles(), HttpStatus.OK);
		return entity;
	}

	@RequestMapping(value = "vehicleOccupancies", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<List<VehicleAndOccupancies>> vehicleOccupancies() {
		ResponseEntity<List<VehicleAndOccupancies>> responseEntity = new ResponseEntity<List<VehicleAndOccupancies>>(
				activityLogger.getVehicleAndOccupancies(), HttpStatus.OK);
		return responseEntity;
	}

	@RequestMapping(value = "occupiedVehicles", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<List<VehicleAndOccupancies>> occupiedVehicles() {

		List<VehicleAndOccupancies> list = new ArrayList<>();
		for (VehicleAndOccupancies vo : activityLogger.getVehicleAndOccupancies())
			if (vo.getOccupancy() != null && vo.getOccupancy().size() > 0)
				list.add(vo);
		ResponseEntity<List<VehicleAndOccupancies>> responseEntity = new ResponseEntity<List<VehicleAndOccupancies>>(
				list, HttpStatus.OK);
		return responseEntity;
	}
	
	@RequestMapping(value = "chatbot", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<ChatBotMessageDto> forChatBot() {
		ChatBotMessageDto list = new ChatBotMessageDto();
		ChatBotTextDto text = new ChatBotTextDto();
		text.setText("Hello Chatbot!!");
		list.getMessages().add(text);
		ResponseEntity<ChatBotMessageDto> responseEntity = new ResponseEntity<ChatBotMessageDto>(
				list, HttpStatus.OK);
		return responseEntity;
	}
}
