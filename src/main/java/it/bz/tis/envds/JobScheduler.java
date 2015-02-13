package it.bz.tis.envds;

import it.siag.services.luft.luft_v1_0.Remark;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;

@Component
public class JobScheduler {

	private static final String[] stationCodes= {"BZ4","BZ5"};
	private static final String[] elementCodes= {"NO2"};
	
	@Autowired
	private WebServiceTemplate webServiceTemplate;

	private EnvironmentPusher pusher;
	private SoapClient client;
	
	public JobScheduler() {
		client = new SoapClient();
		pusher = new EnvironmentPusher();
		pusher.connectToDataCenterCollector();
	}
//	public void getDailyData() throws XmlRpcException {
//		client.initWebServiceTemplate(webServiceTemplate);
//		List<it.bz.tis.envds.Remark> dailyRemarks = new ArrayList<it.bz.tis.envds.Remark>();
//		for (String stationCode : stationCodes){
//			for(String elementCode : elementCodes){
//				Remark latestRemark = client.getDailyData(stationCode,elementCode);
//				dailyRemarks.add(new it.bz.tis.envds.Remark(latestRemark));
//			}
//		}
//		pusher.pushDailyData(dailyRemarks);
//	}

	public void pushHourlyData(){
		try {
			pusher.pushHourlyData(getHourlyData());
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
	public static boolean validateRemark(Remark remark) {
		return remark.getStationCode() !=null && remark.getElementCode()!=null && remark.getDate() != 0 && remark.getValue() != null;
	}
	public List<it.bz.tis.integreen.dto.Remark> getHourlyData() {
		client.initWebServiceTemplate(webServiceTemplate);
		List<it.bz.tis.integreen.dto.Remark> hourlyRemarks = new ArrayList<it.bz.tis.integreen.dto.Remark>();
		for (String stationCode : stationCodes){
			for(String elementCode : elementCodes){
				List<Remark> latestRemarks = client.getHourlyData(stationCode,elementCode);
				for (Remark remark:latestRemarks){
					if (remark == null)
						continue;
					if (validateRemark(remark))
						hourlyRemarks.add(new it.bz.tis.integreen.dto.Remark(remark.getStationCode(),remark.getElementCode(),remark.getDate(),remark.getValue().doubleValue()));
				}
			}
		}
		return hourlyRemarks;
	}
}
