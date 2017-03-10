package it.bz.idm.carsharing;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import it.bz.idm.bdp.dto.SimpleRecordDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CarsharingTest {
	
	Logger logger = LoggerFactory.getLogger(CarsharingTest.class);

	@Autowired
	CarsharingConnector carsharingConnector;

	RestTemplate restTemplate;

	@Before
	public void setUp() {
		restTemplate = new RestTemplate();
	}

	@After
	public void tearDown() {

	}

	@Test
	public void compareWithOldServiceTest() {
		try {
			carsharingConnector.connectForRealTimeData(carsharingConnector.connectForStaticData());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ActivityLogger activityLogger = carsharingConnector.getActivityLogger();
		for (String stationId : activityLogger.getRecords().keySet()) {
			logger.info("TESTING stationId: "+stationId);
			SimpleRecordDto recordDto = restTemplate.getForObject(
					"http://ipchannels.integreen-life.bz.it/carsharingFrontEnd/rest/get-newest-record?station="
							+ stationId,
					SimpleRecordDto.class);
			assertEquals(recordDto.getValue(), activityLogger.getRecords().get(stationId).getValue());
			assertEquals(recordDto.getPeriod(), activityLogger.getRecords().get(stationId).getPeriod());
//			assertEquals(recordDto.getTimestamp(), activityLogger.getRecords().get(stationId).getTimestamp());
		}
	}
}
