package org.openmrs.module.ugandaemrsync.server;

import org.junit.Test;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;

import static org.junit.Assert.*;

public class UgandaEMRHttpURLConnectionTest {
	
	@Test
	public void isConnectionAvailable() {
		UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
		assertTrue(ugandaEMRHttpURLConnection.isConnectionAvailable());
	}
	
	@Test
	public void isServerAvailable() {
		UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
		assertTrue(ugandaEMRHttpURLConnection.isServerAvailable("https://ugisl.mets.or.ug/"));
	}
	
	@Test
	public void serverIsNotAvailable() {
		UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
		assertFalse(ugandaEMRHttpURLConnection.isServerAvailable("http://no-server.exists.ug"));
	}
	
	@Test
	public void shouldBeEqualToTheProvidedBaseURL() {
		UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
		assertEquals("https://ughim.cphluganda.org",
		    ugandaEMRHttpURLConnection.getBaseURL("https://ughim.cphluganda.org/recency/upload/"));
	}
}
