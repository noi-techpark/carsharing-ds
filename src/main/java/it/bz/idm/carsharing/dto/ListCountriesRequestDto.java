package it.bz.idm.carsharing.dto;

import it.bz.idm.carsharing.wsdl.UserAuth;
import it.bz.idm.carsharing.dto.ListCountriesRequestDto.ListCountriesSubRequestDto;
import it.bz.idm.carsharing.dto.RequestDto.SubRequestDto;

public class ListCountriesRequestDto extends RequestDto<ListCountriesSubRequestDto>{
	
	static class ListCountriesSubRequestDto extends SubRequestDto {

	}

	public ListCountriesRequestDto(UserAuth technicaluser)
	   {
	      this.request = new ListCountriesSubRequestDto();
	      this.request.technicalUser = technicaluser;
	      this.function = "Api.ListCountries";
	   }
}
