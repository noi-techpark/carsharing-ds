package it.bz.idm.carsharing.api;

public class StationDto {
	private String id;
	private Double longitude;
	private Double latitude;

	// TODO see if correct
	public static String IDENTIFIER;
	public static String VALUE_IDENTIFIER;
	public static String TIMESTAMP;
	public static String CREATED_ON;
	public static String STATE;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

}
