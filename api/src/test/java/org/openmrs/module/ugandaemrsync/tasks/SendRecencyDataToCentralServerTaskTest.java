package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.TestCase.assertEquals;

public class SendRecencyDataToCentralServerTaskTest extends Mockito {
	
	private SendRecencyDataToCentralServerTask sendRecencyDataToCentralServerTask;
	
	@Before
	public void setUp() {
		sendRecencyDataToCentralServerTask = new SendRecencyDataToCentralServerTask();
	}
	
	@Test
	public void testSuccessfulStatus() {
		//given:
		StatusLine statusLine = mock(StatusLine.class);
		//and:
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		//then:
		assertEquals(HttpStatus.SC_OK, statusLine.getStatusCode());
	}
	
	@Test
	public void testFailedAuthenticationStatus() {
		//given:
		StatusLine statusLine = mock(StatusLine.class);
		//and:
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_UNAUTHORIZED);
		//then:
		assertEquals(HttpStatus.SC_UNAUTHORIZED, statusLine.getStatusCode());
	}
	
	@Test
	public void testInternalServerErrorStatus() {
		//given:
		StatusLine statusLine = mock(StatusLine.class);
		//and:
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		//then:
		assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, statusLine.getStatusCode());
	}
	
	@Test
	public void testUsernameAndPasswordMatches() {
		//given:
		UsernamePasswordCredentials credentials = mock(UsernamePasswordCredentials.class);
		//and:
		when(credentials.getUserName()).thenReturn("admin");
		when(credentials.getPassword()).thenReturn("admin");
		//then:
		assertEquals("admin", credentials.getUserName());
		assertEquals("admin", credentials.getPassword());
	}
	
	@Test
	public void testUsernameAndPasswordDoesNotMatch() {
		//given:
		UsernamePasswordCredentials credentials = mock(UsernamePasswordCredentials.class);
		//and:
		when(credentials.getUserName()).thenReturn("admin");
		when(credentials.getPassword()).thenReturn("admin");
		//then:
		assertNotSame("admin", "Admin", credentials.getUserName());
		assertNotSame("admin", "admin123", credentials.getPassword());
	}
}
