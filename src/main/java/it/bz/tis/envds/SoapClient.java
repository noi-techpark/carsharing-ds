package it.bz.tis.envds;

import it.siag.services.luft.luft_v1_0.Remark;
import it.siag.services.luft.remarks_v1_0.GetHourlyData;
import it.siag.services.luft.remarks_v1_0.GetHourlyDataResponse;

import java.io.IOException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
public class SoapClient {
	private static final String SOAP_ACTION_FOR_HOURLY_DATA = "http://luft.services.siag.it/luft_v1/getHourlyData";
	protected static final String SOAP_ACTION_FOR_DAILY_DATA = "http://luft.services.siag.it/luft_v1/getDailyData";
	private WebServiceTemplate webServiceTemplate;
	private final int TIME_IN_HOURS_TO_GET_LAST_RECORD = 24;
	//private final int TIME_IN_DAYS_TO_GET_LAST_RECORD = 2;
	public SoapClient() {
	}
	
	public List<Remark> getHourlyData(String stationCode, String elementCode) {
		DateTime now = new DateTime();
		GetHourlyData data = new GetHourlyData();
		data.setStationCode(stationCode);
		data.setElememtCode(elementCode);
		data.setRemarkedFrom((now.minusHours(TIME_IN_HOURS_TO_GET_LAST_RECORD).getMillis())/1000);
		data.setRemarkedTo(now.getMillis()/1000);
		Object marshalSendAndReceive = webServiceTemplate.marshalSendAndReceive(data,new WebServiceMessageCallback() {
			@Override
			public void doWithMessage(WebServiceMessage message) throws IOException,
					TransformerException {
				SoapMessage soapMessage = ((SoapMessage) message);
				soapMessage.setSoapAction(SOAP_ACTION_FOR_HOURLY_DATA);
			}
		});
		GetHourlyDataResponse recivedData = (GetHourlyDataResponse)marshalSendAndReceive;
		return recivedData.getRemark();
	}
//	public Remark getDailyData(String stationCode, String elementCode) {
//		DateTime now = new DateTime();
//		GetDailyData data = new GetDailyData();
//		data.setStationCode(stationCode);
//		data.setElememtCode(elementCode);
//		data.setRemarkedFrom(now.minusDays(TIME_IN_DAYS_TO_GET_LAST_RECORD).getMillis()/1000);
//		data.setRemarkedTo(now.getMillis()/1000);
//		Object marshalSendAndReceive = webServiceTemplate.marshalSendAndReceive(data,new WebServiceMessageCallback() {
//			@Override
//			public void doWithMessage(WebServiceMessage message) throws IOException,
//					TransformerException {
//				SoapMessage soapMessage = ((SoapMessage) message);
//				soapMessage.setSoapAction(SOAP_ACTION_FOR_DAILY_DATA);
//			}
//		});
//		GetDailyDataResponse recivedData = (GetDailyDataResponse)marshalSendAndReceive;
//		if (recivedData.getRemark()!=null && recivedData.getRemark().size()>0)
//			return recivedData.getRemark().get(0);
//		return null;
//	}

	public void initWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
		if (this.webServiceTemplate == null)		
			this.webServiceTemplate = webServiceTemplate;
	}
}
