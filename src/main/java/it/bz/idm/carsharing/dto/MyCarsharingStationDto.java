package it.bz.idm.carsharing.dto;

import it.bz.idm.bdp.dto.carsharing.BookMode;
import it.bz.idm.bdp.dto.carsharing.CarsharingStationDto;
import it.bz.idm.bdp.dto.carsharing.Company;
import it.bz.idm.bdp.dto.carsharing.GeoPos;
import it.bz.idm.bdp.dto.carsharing.StationAccess;

public class MyCarsharingStationDto {
	protected String id;
	protected String name;
	protected Double latitude;
	protected Double longitude;
	protected String crs;
	private String origin;
	public static final String IDENTIFIER ="id";
	public static final String VALUE_IDENTIFIER = "free";
	public static final String TIMESTAMP = "timestamp";
	public static final String CREATED_ON = "created_on";
	boolean  hasFixedParking;
	BookMode bookMode;
	StationAccess access;
	Company company;

	public MyCarsharingStationDto() {
		this.setOrigin("CARSHARINGBZ");
	}

	public void setUid(String uid)
	{
		this.setId(uid);
	}

	public void setHasFixedParking(boolean hasFixedParking)
	{
		this.hasFixedParking = hasFixedParking;
	}

	public boolean isHasFixedParking()
	{
		return this.hasFixedParking;
	}

	public void setGeoPos(GeoPos geoPos)
	{
		this.setLatitude(Double.parseDouble(geoPos.getLat()));
		this.setLongitude(Double.parseDouble(geoPos.getLon()));
	}

	public void setBookMode(BookMode bookMode)
	{
		this.bookMode = bookMode;
	}

	public BookMode getBookMode()
	{
		return this.bookMode;
	}

	public StationAccess getAccess() {
		return access;
	}

	public void setAccess(StationAccess access) {
		this.access = access;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public boolean checkIfValid() {
		return this.id != null && !this.id.isEmpty();
	}

	public static CarsharingStationDto[] convertToStationDto(MyCarsharingStationDto[] stations) {
		CarsharingStationDto[] ret = new CarsharingStationDto[stations.length];
		for( int i = 0; i < stations.length; i++){
			MyCarsharingStationDto myCarsharingStationDto = stations[i];
			ret[i] = new CarsharingStationDto();
			ret[i].setAccess(myCarsharingStationDto.getAccess());
			ret[i].setBookMode(myCarsharingStationDto.getBookMode());
			ret[i].setCompany(myCarsharingStationDto.getCompany());
			ret[i].setCrs(myCarsharingStationDto.getCrs());
			ret[i].setHasFixedParking(myCarsharingStationDto.isHasFixedParking());
			ret[i].setId(myCarsharingStationDto.getId());
			ret[i].setLatitude(myCarsharingStationDto.getLatitude());
			ret[i].setLongitude(myCarsharingStationDto.getLongitude());
			ret[i].setName(myCarsharingStationDto.getName());
			ret[i].setOrigin(myCarsharingStationDto.getOrigin());
		}
		return ret;
	}

}
