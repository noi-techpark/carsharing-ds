package it.bz.tis.envds;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import it.bz.tis.integreen.dto.Remark;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/applicationContext*.xml" })
public class App {
	
	@Test
	public void testSoapClient() {
		JobScheduler scheduler = new JobScheduler();
		List<Remark> hourlyData = scheduler.getHourlyData();
		assertNotNull(hourlyData);
		assertFalse(hourlyData.isEmpty());
	}
}
