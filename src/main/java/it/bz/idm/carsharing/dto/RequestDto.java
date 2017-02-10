package it.bz.idm.carsharing.dto;

import it.bz.idm.carsharing.wsdl.UserAuth;
import it.bz.idm.carsharing.dto.RequestDto.SubRequestDto;

public class RequestDto<S extends SubRequestDto> {

	public static class SubRequestDto {
		UserAuth technicalUser = new UserAuth();
		String authKey;

		public String getAuthKey() {
			return authKey;
		}

		public void setAuthKey(String authKey) {
			this.authKey = authKey;
		}

		public void setTechnicalUser(UserAuth technicalUser) {
			this.technicalUser = technicalUser;
		}

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
