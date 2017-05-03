package it.bz.idm.carsharing;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class CarsharingIT {
	@Autowired
	private CarsharingConnector carsharingConnector;

	@Autowired
	private CarsharingStationSync stationPusher;

	@Autowired
	private CarsharingCarSync carPusher;

	RestTemplate restTemplate;

	@Before
	public void setUp() {
		restTemplate = new RestTemplate();
	}

//	@Test
//	public void testEndpoints() {
//		try {
//			HashMap<String, List<String>> vehicleIdsByStationIds = carsharingConnector
//					.connectForStaticData(stationPusher, carPusher);
//			assertNotNull(vehicleIdsByStationIds);
//			carsharingConnector.connectForRealTimeData(vehicleIdsByStationIds, stationPusher, carPusher);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	// to compare with existing service, but activitylogger doesn exist anymore
	// TODO find avtivityLogger alternative

	// @Test
	// public void compareWithOldServiceTest() {
	// try {
	// carsharingConnector.connectForRealTimeData(
	// carsharingConnector.connectForStaticData(stationPusher, carPusher),
	// stationPusher, carPusher);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// for (String stationId : activityLogger.getRecords().keySet()) {
	// SimpleRecordDto recordDto = restTemplate.getForObject(
	// "http://ipchannels.integreen-life.bz.it/carsharingFrontEnd/rest/get-newest-record?station="
	// + stationId,
	// SimpleRecordDto.class);
	// assertEquals(recordDto.getValue(),
	// activityLogger.getRecords().get(stationId).getValue());
	// assertEquals(recordDto.getPeriod(),
	// activityLogger.getRecords().get(stationId).getPeriod());
	// }
	// }

}
