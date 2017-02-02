package it.bz.idm.carsharing;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import it.bz.idm.carsharing.dto.ListCountriesRequestDto;
import it.bz.idm.carsharing.dto.ListCountriesResponseDto;
import it.bz.idm.carsharing.wsdl.BoundingBox;
import it.bz.idm.carsharing.wsdl.ListStationsByGeoPosRequest;
import it.bz.idm.carsharing.wsdl.ListStationsByGeoPosResponse;
import it.bz.idm.carsharing.wsdl.UserAuth;

public class Hal2ApiClient extends WebServiceGatewaySupport {

	public ListStationsByGeoPosResponse getStationsByGeoPos(String endpoint, BoundingBox boundingBox, UserAuth auth) {
		ListStationsByGeoPosResponse ret = null;

		ListStationsByGeoPosRequest request = new ListStationsByGeoPosRequest();
		request.setTechnicalUser(auth);
		request.setBoundingBox(boundingBox);
		// TODO ??
		// request.setAuthKey(value);
		ret = (ListStationsByGeoPosResponse) getWebServiceTemplate().marshalSendAndReceive(endpoint, request);

		return ret;
	}
	
	public ListCountriesResponseDto getCountry(String endpoint,UserAuth auth) {
		ListCountriesResponseDto ret = null;

		ListCountriesRequestDto request = new ListCountriesRequestDto(auth);
		// TODO ??
		// request.setAuthKey(value);
		ret = (ListCountriesResponseDto) getWebServiceTemplate().marshalSendAndReceive(endpoint, request);
		return ret;
	}
}
