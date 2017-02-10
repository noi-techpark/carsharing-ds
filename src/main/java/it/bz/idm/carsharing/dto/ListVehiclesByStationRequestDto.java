package it.bz.idm.carsharing.dto;

import it.bz.idm.carsharing.dto.RequestDto.SubRequestDto;
import it.bz.idm.carsharing.wsdl.UserAuth;

import java.util.List;

import it.bz.idm.carsharing.dto.ListVehiclesByStationRequestDto.ListVehiclesByStationSubRequestDto;

public class ListVehiclesByStationRequestDto extends RequestDto<ListVehiclesByStationSubRequestDto> {
	public class ListVehiclesByStationSubRequestDto extends SubRequestDto {
		String[] stationUID;

		public String[] getStationUID() {
			return this.stationUID;
		}
	}

	public ListVehiclesByStationRequestDto(UserAuth userAuth, List<String> staionIds) {
		this.request = new ListVehiclesByStationSubRequestDto();
		this.request.technicalUser = userAuth;
		this.request.stationUID = staionIds.toArray(new String[staionIds.size()]);
		this.function = "Api.listVehiclesByStation";
	}
}
