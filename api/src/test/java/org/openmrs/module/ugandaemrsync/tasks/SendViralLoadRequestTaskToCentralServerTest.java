package org.openmrs.module.ugandaemrsync.tasks;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_SEND_SAMPLE_FHIR_JSON_STRING;

public class SendViralLoadRequestTaskToCentralServerTest extends BaseModuleContextSensitiveTest {
	
	@Test
	public void convertToFHIRMap() {
		SendViralLoadRequestToCentralServerTask sendViralLoadRequestToCentralServerTask = new SendViralLoadRequestToCentralServerTask();
		sendViralLoadRequestToCentralServerTask.generateVLFHIRTestRequestBody(new Encounter(),
		    VL_SEND_SAMPLE_FHIR_JSON_STRING);
	}
	
}
