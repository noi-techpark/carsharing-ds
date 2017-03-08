package it.bz.idm.carsharing;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import it.bz.idm.carsharing.wsdl.NeighborStation;

/**
 * HALAPI sends distance from GetStationREsponse in Strin format : 6 km. This
 * class converts the string to float
 * 
 * @author sdalvai
 *
 */
public class MyNeighborStationDeserializer extends JsonDeserializer<NeighborStation> {

	@Override
	public NeighborStation deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		NeighborStation ret = new NeighborStation();

		ObjectCodec oc = p.getCodec();
		JsonNode node = oc.readTree(p);
		int uid = node.get("uid").asInt();
		String name = node.get("name").asText();
		String shorthand = node.get("shorthand").asText();
		String distance = node.get("distance").asText();
		float realDistance = -1;
		if (distance != null) {
			distance.replaceAll(" km", "");
			realDistance = Float.parseFloat(distance);
		}
		if (name != null)
			ret.setName(name);
		if (shorthand != null)
			ret.setShorthand(shorthand);
//		if (realDistance > 0)
//			ret.setDistance(realDistance);
		ret.setUid(uid);
		return ret;
	}

}
