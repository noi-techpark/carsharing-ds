package it.bz.idm.carsharing.dto;

import java.util.List;

import it.bz.idm.carsharing.dto.GetStationRequestDto.GetStationSubRequestDto;
import it.bz.idm.carsharing.dto.RequestDto.SubRequestDto;
import it.bz.idm.carsharing.wsdl.UserAuth;

public class GetStationRequestDto extends RequestDto<GetStationSubRequestDto> {
	public class GetStationSubRequestDto extends SubRequestDto {
		String[] stationUID;

		public String[] getStationUID() {
			return this.stationUID;
		}
	}

	public GetStationRequestDto(UserAuth userAuth, List<String> staionIds) {
		this.request = new GetStationSubRequestDto();
		this.request.technicalUser = userAuth;
		this.function = "Api.getStation";
		this.request.stationUID = staionIds.toArray(new String[staionIds.size()]);
	}
}
