package it.bz.idm.carsharing.dto;

import it.bz.idm.carsharing.wsdl.BoundingBox;
import it.bz.idm.carsharing.wsdl.UserAuth;
import it.bz.idm.carsharing.dto.ListStationsByGeoPosRequestDto.ListStationByGeoPosSubRequestDto;
import it.bz.idm.carsharing.dto.RequestDto.SubRequestDto;

public class ListStationsByGeoPosRequestDto extends RequestDto<ListStationByGeoPosSubRequestDto> {

	static class ListStationByGeoPosSubRequestDto extends SubRequestDto {
		BoundingBox boundingBox;

		public BoundingBox getBoundingBox() {
			return boundingBox;
		}

		public void setBoundingBox(BoundingBox boundingBox) {
			this.boundingBox = boundingBox;
		}

	}

	public ListStationsByGeoPosRequestDto(BoundingBox box, UserAuth technicaluser) {
		this.request = new ListStationByGeoPosSubRequestDto();
		this.request.boundingBox = box;
		this.request.technicalUser = technicaluser;
		this.function = "Api.listStationsByGeoPos";
	}
}
