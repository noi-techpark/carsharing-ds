package it.bz.idm.carsharing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class HAL2ApiClientConfiguration {
	@Bean
	public Jaxb2Marshaller marshaller() {
		final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		// this package must match the package in the <generatePackage>
		// specified in
		// pom.xml
		marshaller.setContextPath("it.bz.idm.carsharing.wsdl");
		return marshaller;
	}
	
	@Bean
	public Hal2ApiClient hal2ApiClient(Jaxb2Marshaller marshaller) {
		final Hal2ApiClient client = new Hal2ApiClient();
		client.setDefaultUri("https://xml.dbcarsharing-buchung.de/hal2_api/hal2_api_3.php?protocol=json");
		client.setMarshaller(marshaller);
		client.setUnmarshaller(marshaller);
		return client;
	}
}
