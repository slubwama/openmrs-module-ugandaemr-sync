/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync.api.impl;

import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.ObsService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.api.dao.UgandaEMRSyncDao;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.GP_DHIS2;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.PATIENT_IDENTIFIER_TYPE;

public class UgandaEMRSyncServiceImpl extends BaseOpenmrsService implements UgandaEMRSyncService {
	
	UgandaEMRSyncDao dao;
	
	@Autowired
	UserService userService;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setDao(UgandaEMRSyncDao dao) {
		this.dao = dao;
	}
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	/**
	 * @return
	 * @throws APIException
	 */
	@Override
	public List<SyncTaskType> getAllSyncTaskType() throws APIException {
		return dao.getAllSyncTaskType();
	}
	
	/**
	 * @param uuid
	 * @return
	 * @throws APIException
	 */
	@Override
	public SyncTaskType getSyncTaskTypeByUUID(String uuid) throws APIException {
		return dao.getSyncTaskTypeByUUID(uuid);
	}
	
	/**
	 * @param syncTaskType
	 * @return
	 * @throws APIException
	 */
	@Override
	public SyncTaskType saveSyncTaskType(SyncTaskType syncTaskType) throws APIException {
		if (syncTaskType.getCreator() == null) {
			syncTaskType.setCreator(userService.getUser(1));
		}
		return dao.saveSyncTaskType(syncTaskType);
	}
	
	@Override
	public SyncTask getSyncTask(int syncTask) throws APIException {
		return dao.getSyncTask(syncTask);
	}
	
	/**
	 * Get SyncTask
	 * 
	 * @return
	 * @throws APIException
	 */
	@Transactional
	public List<SyncTask> getAllSyncTask() {
		return dao.getAllSyncTask();
	}
	
	/**
	 * @param syncTask
	 * @return
	 * @throws APIException
	 */
	@Override
	public SyncTask saveSyncTask(SyncTask syncTask) throws APIException {
		if (syncTask.getCreator() == null) {
			syncTask.setCreator(userService.getUser(1));
		}
		return dao.saveSyncTask(syncTask);
	}
	
	@Override
	public List<SyncTask> getIncompleteActionSyncTask() throws APIException {
		return dao.getIncompleteActionSyncTask();
	}
	
	/**
	 * @param query
	 * @return
	 */
	@Override
	public List getDatabaseRecord(String query) {
		return dao.getDatabaseRecord(query);
	}
	
	/**
	 * @param columns
	 * @param finalQuery
	 * @return
	 */
	@Override
	public List getFinalList(List<String> columns, String finalQuery) {
		return dao.getFinalResults(columns, finalQuery);
	}
	
	public Encounter addVLToEncounter(String qualitativeVl, String quantitativeVl, String vlDate, Encounter encounter) {
		
		Concept dateSampleTaken = Context.getConceptService().getConcept("163023");
		Concept viralLoadQualitative = Context.getConceptService().getConcept("1305");
		Concept viralLoadQuantitative = Context.getConceptService().getConcept("856");
		Concept valueCoded = null;
		
		String dateFormat = getDateFormat(vlDate);
		
		String vlQualitativeString = qualitativeVl.replaceAll("\"", "");
		
		if (vlQualitativeString.contains("Target Not Detected") || vlQualitativeString.contains("Not detected")) {
			valueCoded = Context.getConceptService().getConcept("1306");
		} else if (vlQualitativeString.contains("FAILED")) {
			valueCoded = Context.getConceptService().getConcept("1304");
		} else {
			valueCoded = Context.getConceptService().getConcept("1301");
		}
		
		Obs obs = createObs(encounter, dateSampleTaken, null, convertStringToDate(vlDate, "00:00:00", dateFormat), null);
		Obs obs1 = createObs(encounter, viralLoadQualitative, valueCoded, null, null);
		Obs obs2 = createObs(encounter, viralLoadQuantitative, null, null, Double.valueOf(quantitativeVl));
		
		//Void Similar observation
		voidObsFound(encounter, dateSampleTaken);
		voidObsFound(encounter, viralLoadQualitative);
		voidObsFound(encounter, viralLoadQuantitative);
		
		encounter.addObs(obs);
		encounter.addObs(obs1);
		encounter.addObs(obs2);
		
		return Context.getEncounterService().saveEncounter(encounter);
		
	}
	
	public String getDateFormat(String date) {
		String dateFormat = "";
		if (date.contains("-")) {
			dateFormat = "yyyy-MM-dd";
		} else if (date.contains("/")) {
			dateFormat = "dd/MM/yyyy";
		}
		return dateFormat;
	}
	
	public Patient getPatientByPatientIdentifier(String patientId) {
		try {
			return Context.getPatientService().getPatientIdentifiers(patientId, null, null, null, null).get(0).getPatient();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public boolean validateFacility(String facilityDHIS2UUID) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(GP_DHIS2);
		return facilityDHIS2UUID.contentEquals(globalProperty);
	}
	
	public Collection<EncounterType> getEcounterTypes(String encounterTypesUUID) {
        Collection<EncounterType> encounterTypes = new ArrayList<>();
        encounterTypes.add(Context.getEncounterService().getEncounterTypeByUuid(encounterTypesUUID));
        return encounterTypes;
    }
	
	public Date convertStringToDate(String string, String time, String dateFormat) {
		DateFormat format = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
		Date date = null;
		
		try {
			date = format.parse(string);
			if (date != null && time != "") {
				date = dateFormtter(date, time);
			}
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		
		return date;
	}
	
	private Date dateFormtter(Date date, String time) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		SimpleDateFormat formatterExt = new SimpleDateFormat("dd/MM/yyyy");
		
		String formattedDate = formatterExt.format(date) + " " + time;
		
		return formatter.parse(formattedDate);
	}
	
	private Obs createObs(Encounter encounter, Concept concept, Concept valueCoded, Date valueDatetime, Double valueNumeric) {
		Obs newObs = new Obs();
		newObs.setConcept(concept);
		newObs.setValueCoded(valueCoded);
		newObs.setValueNumeric(valueNumeric);
		newObs.setValueDatetime(valueDatetime);
		newObs.setCreator(encounter.getCreator());
		newObs.setDateCreated(encounter.getDateCreated());
		newObs.setEncounter(encounter);
		newObs.setPerson(encounter.getPatient());
		return newObs;
	}
	
	private void voidObsFound(Encounter encounter, Concept concept) {
		ObsService obsService = Context.getObsService();
		List<Obs> obsList = obsService.getObservationsByPersonAndConcept(encounter.getPatient(), concept);
		for (Obs obs1 : obsList) {
			if (obs1.getEncounter() == encounter) {
				obsService.voidObs(obs1, "Replaced with a new one because it was changed");
			}
		}
	}
	
	public String getHealthCenterCode() {
		SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
		return syncGlobalProperties.getGlobalProperty(GP_DHIS2);
	}
	
	public String getHealthCenterName() {
		SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
		return syncGlobalProperties.getGlobalProperty("aijar.healthCenterName");
	}
	
	public String getHealthCenterViralLoadToken() {
		SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
		return syncGlobalProperties.getGlobalProperty("ugandaemr.viralload.token");
	}
	
	public String getPatientARTNO(Patient patient) {
		String query = "select patient_identifier.identifier from patient_identifier inner join patient_identifier_type on(patient_identifier.identifier_type=patient_identifier_type.patient_identifier_type_id) where patient_identifier_type.uuid in ('"
		        + PATIENT_IDENTIFIER_TYPE + "') AND patient_id=" + patient.getPatientId() + "";
		
		List list = Context.getAdministrationService().executeSQL(query, true);
		String patientARTNO = "";
		
		if (list.size() > 0) {
			patientARTNO = list.get(0).toString().replace("[", "").replace("]", "");
		}
		return patientARTNO;
	}
}
