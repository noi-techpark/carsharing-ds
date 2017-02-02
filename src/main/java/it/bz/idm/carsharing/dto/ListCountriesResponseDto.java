package it.bz.idm.carsharing.dto;

import java.util.ArrayList;

public class ListCountriesResponseDto {
	ArrayList<CountryDto> country;

	public ArrayList<CountryDto> getCountry() {
		return country;
	}

	public void setCountry(ArrayList<CountryDto> country) {
		this.country = country;
	}
}
