package it.bz.idm.carsharing.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.bz.idm.carsharing.wsdl.Station;

//@XmlSeeAlso(it.bz.idm.carsharing.wsdl.ListStationsByGeoPosResponse.class)
//@XmlRootElement(name="ListStationsByGeoPosResponseDto")
public class ListStationsByGeoPosResponseDto implements Serializable{
	
	public ListStationsByGeoPosResponseDto(){
		
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected List<Station> station;

    /**
     * Gets the value of the station property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the station property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Station }
     * 
     * 
     */
    public List<Station> getStation() {
        if (station == null) {
            station = new ArrayList<Station>();
        }
        return this.station;
    }
}
