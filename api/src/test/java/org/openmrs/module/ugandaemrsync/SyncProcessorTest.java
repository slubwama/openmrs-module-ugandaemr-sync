/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync;

import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.module.ugandaemrsync.server.SyncConstant;
import org.openmrs.module.ugandaemrsync.server.SyncDataRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This is a unit test, which verifies logic in UgandaEMRSyncService. It doesn't extend
 * BaseModuleContextSensitiveTest, thus it is run without the in-memory DB and Spring context.
 */
public class SyncProcessorTest extends Mockito {
	
	@Test
	public void shouldConvertListToJsonString() throws IOException {
		List<Object[]> objects = new ArrayList<Object[]>();
		
		Object[] encounters = { "1", 2, "7" };
		objects.add(encounters);
		List<String> columns = Arrays.asList("encounter_id", "person_id", "value_text");
		
		Map<String, String> dt = SyncDataRecord.convertListOfMapsToJsonString(objects, columns);
		String result = dt.get("json");
		assertNotNull(result.contains("1"));
		
	}
	
	@Test
	public void shouldReplaceFacilityAndLimitsInQueryString() {
		String facilityId = "123332323";
		String limitFrom = "1";
		String limitTo = "10";
		
		String personQuery = SyncConstant.ENCOUNTER_QUERY;
		String lastSyncDate = "2010-01-01 12:01:01";
		String personQueryWith = String.format(personQuery, facilityId, lastSyncDate, lastSyncDate, lastSyncDate, limitFrom,
		    limitTo);
		
		/*Session session = Context.getRegisteredComponent("sessionFactory", SessionFactory.class).getCurrentSession();

		SQLQuery query = session.createSQLQuery(personQueryWith);
		List results = query.list();
		*/
		assertTrue(personQueryWith.contains("10"));
		//assertTrue(results.size() > 0);
		
	}
	
	@Test
	public void shouldTestIndex() {
		int startIndex = 0;
		boolean entireListNotProcessed = true;
		int mySize = 30;
		int offset = 0;
		while (entireListNotProcessed) {
			
			if (offset >= mySize || mySize <= 500) {
				entireListNotProcessed = false;
			} else {
				startIndex = startIndex + 1;
			}
			offset = (startIndex * 500);
			
		}
		
	}
	
	@Test
	public void shouldTestReplace() {
		String personQuery = SyncConstant.TABLES_TOTAL_QUERY;
		
		String lastSyncDate = "2010-01-01 12:01:01";
		
		String allThree = personQuery.replaceAll("lastSync", lastSyncDate);
		
		assertTrue(allThree.contains("2010-01-01"));
	}
}
