package it.bz.idm.carsharing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CarsharingConnectorConfiguration {
	private String user;
	private String password;
	private String endpoint;
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	@Autowired
    public CarsharingConnectorConfiguration(@Value("${cred.user}") String user) {
        this.user = user;
    }
}
