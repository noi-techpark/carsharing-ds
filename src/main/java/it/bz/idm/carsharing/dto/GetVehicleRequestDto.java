package it.bz.idm.carsharing.dto;

import it.bz.idm.carsharing.dto.RequestDto.SubRequestDto;
import it.bz.idm.carsharing.wsdl.UserAuth;

import java.util.List;

import it.bz.idm.carsharing.dto.GetVehicleRequestDto.GetVehicleSubRequest;;

public class GetVehicleRequestDto extends RequestDto<GetVehicleSubRequest> {
	public class GetVehicleSubRequest extends SubRequestDto {
		String[] vehicleUID;

		public String[] getVehicleUID() {
			return this.vehicleUID;
		}
	}

	public GetVehicleRequestDto(UserAuth userAuth, List<String> vehicleIds) {
		this.request = new GetVehicleSubRequest();
		this.request.technicalUser = userAuth;
		this.request.vehicleUID = vehicleIds.toArray(new String[vehicleIds.size()]);
		this.function = "Api.getVehicle";
	}

}
