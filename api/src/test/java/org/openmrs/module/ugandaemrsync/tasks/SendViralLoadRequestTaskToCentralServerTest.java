package org.openmrs.module.ugandaemrsync.tasks;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.Properties;

import static junit.framework.TestCase.assertTrue;

public class SendViralLoadRequestTaskToCentralServerTest extends BaseModuleContextSensitiveTest {
	
	@Override
	public Boolean useInMemoryDatabase() {
		return false;
	}
	
	/**
	 * @return MS Note: use port 3306 as standard, 5538 for sandbox 5.5 mysql environment
	 */
	@Override
	public Properties getRuntimeProperties() {
		System.setProperty(
		    "databaseUrl",
		    "jdbc:mysql://localhost:3306/flagtest?autoReconnect=true&sessionVariables=storage_engine%3DInnoDB&useUnicode=true&characterEncoding=UTF-8");
		System.setProperty("databaseUsername", "root");
		System.setProperty("databasePassword", "yourname");
		System.setProperty("databaseDriver", "com.mysql.jdbc.Driver");
		System.setProperty("databaseDialect", "org.hibernate.dialect.MySQLDialect");
		
		return super.getRuntimeProperties();
	}
	
	private SendViralLoadRequestToCentralServerTask sendViralLoadRequestTaskToCentralServer;
	
	@Before
	public void setUp() {
		sendViralLoadRequestTaskToCentralServer = new SendViralLoadRequestToCentralServerTask();
	}
	
	@Test
	public void testTaskSending() throws Exception {
		sendViralLoadRequestTaskToCentralServer.execute();
		assertTrue(true);
		
	}
	
}
