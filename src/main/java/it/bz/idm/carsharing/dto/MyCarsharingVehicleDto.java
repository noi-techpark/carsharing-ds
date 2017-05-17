package it.bz.idm.carsharing.dto;

import it.bz.idm.bdp.dto.carsharing.CarsharingVehicleDto;

public class MyCarsharingVehicleDto {
	protected String id;
	protected String name;
	protected Double latitude;
	protected Double longitude;
	protected String crs;
	private String origin;

	public static final String IDENTIFIER = "id";
	public static final String STATE = "state";
	public static final String CREATED_ON = "created_on";
	public static final String TIMESTAMP = "timestamp";

	String licensePlate;
	String model;
	String brand;
	String showType;
	String stationId;

	public MyCarsharingVehicleDto() {
		setOrigin("CARSHARINGBZ");
	}

	public void setVehicleUID(String vehicleUID) {
		this.setId(vehicleUID);
	}

	public String getLicensePlate() {
		return this.licensePlate;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getBrand() {
		return this.brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getShowType() {
		return this.showType;
	}

	public void setShowType(String showType) {
		this.showType = showType;
	}

	public String getStationId() {
		return stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
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

	public static CarsharingVehicleDto[] convertToStationDto(MyCarsharingVehicleDto[] vehicles) {
		CarsharingVehicleDto[] ret = new CarsharingVehicleDto[vehicles.length];
		for (int i = 0; i < vehicles.length; i++) {
			MyCarsharingVehicleDto myCarsharingVehicleDto = vehicles[i];
			ret[i] = new CarsharingVehicleDto();
			ret[i].setBrand(myCarsharingVehicleDto.getBrand());
			ret[i].setCrs(myCarsharingVehicleDto.getCrs());
			ret[i].setId(myCarsharingVehicleDto.getId());
			ret[i].setLatitude(myCarsharingVehicleDto.getLatitude());
			ret[i].setLongitude(myCarsharingVehicleDto.getLongitude());
			ret[i].setModel(myCarsharingVehicleDto.getModel());
			ret[i].setName(myCarsharingVehicleDto.getName());
			ret[i].setOrigin(myCarsharingVehicleDto.getOrigin());
			ret[i].setShowType(myCarsharingVehicleDto.getShowType());
			ret[i].setStationId(myCarsharingVehicleDto.getStationId());
		}
		return ret;
	}
}
