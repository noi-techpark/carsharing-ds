package it.bz.idm.carsharing.dto;

public class BoundingBoxDto {
	protected GeoPosDto geoPosWS;
	protected GeoPosDto geoPosEN;

	public BoundingBoxDto(double latWS, double lonWS, double latEN, double lonEN) {
		this.geoPosWS = new GeoPosDto(lonWS, latWS);
		this.geoPosEN = new GeoPosDto(lonEN, latEN);
	}

	public GeoPosDto getGeoPosDtoWS() {
		return geoPosWS;
	}

	public void setGeoPosDtoWS(GeoPosDto geoPosWS) {
		this.geoPosWS = geoPosWS;
	}

	public GeoPosDto getGeoPosDtoEN() {
		return geoPosEN;
	}

	public void setGeoPosDtoEN(GeoPosDto geoPosEN) {
		this.geoPosEN = geoPosEN;
	}

}
