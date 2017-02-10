package it.bz.idm.carsharing.dto;

public class GeoPosDto {
	protected double lon;
	protected double lat;

	public GeoPosDto(double lon, double lat) {
		this.lon = lon;
		this.lat = lat;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public double getLat() {
		return lat;
	}
}
