package it.bz.idm.carsharing.dto;

import java.util.List;

import it.bz.idm.carsharing.dto.RequestDto.SubRequestDto;
import it.bz.idm.carsharing.wsdl.UserAuth;
import it.bz.idm.carsharing.dto.ListVehicleOccupancyByStationRequestDto.ListVehicleOccupancyByStationSubRequestDto;

public class ListVehicleOccupancyByStationRequestDto extends RequestDto<ListVehicleOccupancyByStationSubRequestDto> {
	public class ListVehicleOccupancyByStationSubRequestDto extends SubRequestDto {
		String stationUID;
		String[] vehicleUID;
		String begin;
		String end;

		public String getStationUID() {
			return this.stationUID;
		}

		public String[] getVehicleUID() {
			return this.vehicleUID;
		}

		public String getBegin() {
			return this.begin;
		}

		public String getEnd() {
			return this.end;
		}
	}

	public ListVehicleOccupancyByStationRequestDto(UserAuth userAuth, String begin, String end, String stationUID,
			List<String> vehicleUID) {
		this.request = new ListVehicleOccupancyByStationSubRequestDto();
		this.request.technicalUser = userAuth;
		this.request.begin = begin;
		this.request.end = end;
		this.request.vehicleUID = vehicleUID.toArray(new String[vehicleUID.size()]);
		this.request.stationUID = stationUID;
		this.function = "Api.listVehicleOccupancyByStation";
	}
}
