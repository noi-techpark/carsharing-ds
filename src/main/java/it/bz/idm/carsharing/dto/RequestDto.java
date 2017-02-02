package it.bz.idm.carsharing.dto;

import it.bz.idm.carsharing.wsdl.UserAuth;
import it.bz.idm.carsharing.dto.RequestDto.SubRequestDto;

public class RequestDto<S extends SubRequestDto> {

	public static class SubRequestDto {
		UserAuth technicalUser = new UserAuth();

		public UserAuth getTechnicalUser() {
			return this.technicalUser;
		}
	}

	S request;
	String function = "";

	public S getRequest() {
		return this.request;
	}

	public String getFunction() {
		return this.function;
	}
}
