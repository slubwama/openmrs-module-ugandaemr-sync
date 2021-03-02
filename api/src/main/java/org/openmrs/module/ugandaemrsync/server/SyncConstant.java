/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ugandaemrsync.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SyncConstant {

    public static final String SERVER_IP = "ugandaemrsync.serverIP";

    public static final String SERVER_USERNAME = "ugandaemrsync.server.username";

    public static final String SERVER_PASSWORD = "ugandaemrsync.server.password";

    public static final String HEALTH_CENTER_SYNC_ID = "ugandaemrsync.healthCenterSyncId";

    public static final String SERVER_PROTOCOL = "ugandaemrsync.protocol";

    public static final String INITIAL_SYNC = "ugandaemrsync.init";

    public static final String INITIAL_SYNC_PLACE_HOLDER = "false";

    public static final String SERVER_PROTOCOL_PLACE_HOLDER = "http://";

    public static final String SERVER_IP_PLACE_HOLDER = "192.168.1.28";

    public static final String HEALTH_CENTER_SYNC_ID_PLACE_HOLDER = "This will be filled when a connection is created to server";

    public static final int CONNECTION_SUCCESS_200 = 200;

    public static final int CONNECTION_SUCCESS_201 = 201;

    public static final String XML_CONTENT_TYPE = "application/xml";

    public static final String JSON_CONTENT_TYPE = "application/json";

    public static final String LAST_SYNC_DATE = "ugandaemr.lastSyncDate";
    public static final String GP_ENABLE_SYNC_CBS_FHIR_DATA = "ugandaemrsync.syncCBSFHIRData";
    public static final String GP_CBS_LOCALHOST_PORT = "ugandaemrsync.CBSLocalHostPort";
    public static final String GP_CBS_LOCALHOST_USERNAME = "ugandaemrsync.CBSLocalHostUsername";
    public static final String GP_CBS_LOCALHOST_PASSWORD = "ugandaemrsync.CBSLocalHostPassword";

    public static Date MINIMUM_DATE = new Date(Long.MIN_VALUE);

    public static DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

    public static final String LAST_SYNC_DATE_PLACE_HOLDER = "1900-01-01 00:00:00";

    public static final String MAX_NUMBER_OF_ROWS = "ugandaemr.maxNumberOfRows";

    public static final String MAX_NUMBER_OF_ROWS_PLACE_HOLDER = "500";

    public static final String HIV_ENCOUNTER_PAGE_UUID = "8d5b2be0-c2cc-11de-8d13-0010c6dffd0f";

    public static final String VIRAL_LOAD_LAB_REQUEST_ENCOUNTER_TYPE_UUID = "077c43ee-9745-11e9-bc42-526af7764f64";

    public static final String VIRAL_LOAD_SYNC_TASK_TYPE_IDENTIFIER = "315124004";

    public static final String VIRAL_LOAD_SYNC_TYPE_UUID = "3551ca84-06c0-432b-9064-fcfeefd6f4ec";
    public static final String FHIRSERVER_SYNC_TASK_TYPE_UUID = "3c1ce940-8ade-11ea-bc55-0242ac130003";
    public static final String VIRAL_LOAD_RESULT_PULL_TYPE_UUID = "3396dcf0-2106-4e73-9b90-c63978c3a8b4";

    public static final String PATIENT_IDENTIFIER_TYPE = "e1731641-30ab-102d-86b0-7a5022ba4115";

    public static final int VL_SAMPLE_ID_CELL_NO = 1;

	public static final int VL_FACILITY_NAME_CELL_NO = 2;
	public static final int VL_FACILITY_DHIS2_ID_CELL_NO = 4;
	public static final int VL_PATIENT_ART_ID_CELL_NO = 5;

    public static final int VL_DATE_COLLECTION_CELL_NO = 6;

    public static final int VL_RESULTS_NUMERIC_CELL_NO = 8;

    public static final int VL_RESULTS_ALHPA_NUMERIC_CELL_NO = 9;

    public static final int VL_SUPPRESSED_CELL_NO = 11;

    public static final String GP_DHIS2 = "ugandaemr.dhis2.organizationuuid";

    public static final String VL_SEND_SAMPLE_FHIR_JSON_STRING = "{\"resourceType\":\"ServiceRequest\",\"locationCode\":\"%s\",\"locationReference\":\"%s\",\"code\":\"%s\",\"performerType\":\"????\",\"status\":\"active\",\"intent\":\"order\",\"subject\":{\"resourceType\":\"Location\",\"name\":\"%s\"},\"specimen\":[{\"subject\":{\"resourceType\":\"Patient\",\"identifier\":\"%s\"},\"resourceType\":\"Specimen\",\"identifier\":\"%s\",\"type\":\"%s\",\"status\":\"available\",\"collection\":{\"collectedDateTime\":\"%s\",\"collector\":{\"resourceType\":\"Practitioner\",\"name\":\"%s\",\"telecom\":\"%s\"}}}],\"requester\":{\"resourceType\":\"Practitioner\",\"name\":\"%s\",\"telecom\":\"%s\"},\"performer\":[{\"resourceType\":\"Organization\",\"endpoint\":\"%s\"}]}";

    public static final String VL_RECEIVE_RESULT_FHIR_JSON_STRING = "{\"resourceType\":\"ServiceRequest\",\"locationCode\":\"%s\",\"subject\":{\"resourceType\":\"Location\",\"name\":\"UgandaEMR\"},\"specimen\":[{\"subject\":{\"resourceType\":\"Patient\",\"identifier\":\"%s\"},\"resourceType\":\"Specimen\",\"identifier\":\"%s\"}]}";

    public static final String PERSON_QUERY = "SELECT\n" + "  gender,\n" + "  birthdate,\n" + "  birthdate_estimated,\n"
	        + "  dead,\n" + "  death_date,\n" + "  (SELECT c.uuid\n" + "   FROM concept c\n"
	        + "   WHERE c.concept_id = p.cause_of_death) AS cause_of_death,\n" + "  (SELECT u.uuid\n" + "   FROM users u\n"
	        + "   WHERE u.user_id = p.creator)           AS creator,\n" + "  date_created,\n" + "  (SELECT u.uuid\n"
	        + "   FROM users u\n" + "   WHERE u.user_id = p.changed_by)        AS changed_by,\n" + "  date_changed,\n"
	        + "  voided,\n" + "  (SELECT u.uuid\n" + "   FROM users u\n"
	        + "   WHERE u.user_id = p.voided_by)         AS voided_by,\n" + "  date_voided,\n" + "  void_reason,\n"
	        + "  uuid,\n" + "  '%s'                                    AS facility,\n" + "  (CASE\n"
	        + "   WHEN p.date_voided = GREATEST(COALESCE(p.date_created, '1000-01-01'),\n"
	        + "                                 COALESCE(p.date_changed, '1000-01-01'),\n"
	        + "                                 COALESCE(p.date_voided, '1000-01-01'))\n" + "     THEN 'VOIDED'\n"
	        + "   WHEN p.date_changed = GREATEST(COALESCE(p.date_created, '1000-01-01'),\n"
	        + "                                  COALESCE(p.date_changed, '1000-01-01'),\n"
	        + "                                  COALESCE(p.date_voided, '1000-01-01'))\n" + "     THEN 'UPDATED'\n"
	        + "   ELSE 'NEW'\n" + "   END)                            AS state,\n" + "  deathdate_estimated,\n"
	        + "  birthtime\n" + "FROM person p\n"
	        + "WHERE p.date_created > '%s' OR p.date_changed > '%s' OR p.date_voided > '%s'\n" + "LIMIT %s, %s";
	
	public static final String PERSON_ADDRESS_QUERY = "SELECT\n" + "  (SELECT p.uuid\n" + "   FROM person AS p\n"
	        + "   WHERE p.person_id = pa.person_id) AS person,\n" + "  preferred,\n" + "  address1,\n" + "  address2,\n"
	        + "  city_village,\n" + "  state_province,\n" + "  postal_code,\n" + "  country,\n" + "  latitude,\n"
	        + "  longitude,\n" + "  start_date,\n" + "  end_date,\n" + "  (SELECT u.uuid\n" + "   FROM users u\n"
	        + "   WHERE u.user_id = pa.creator)     AS creator,\n" + "  date_created,\n" + "  voided,\n"
	        + "  (SELECT u.uuid\n" + "   FROM users u\n" + "   WHERE u.user_id = pa.voided_by)   AS voided_by,\n"
	        + "  date_voided,\n" + "  void_reason,\n" + "  county_district,\n" + "  address3,\n" + "  address4,\n"
	        + "  address5,\n" + "  address6,\n" + "  date_changed,\n" + "  (SELECT u.uuid\n" + "   FROM users u\n"
	        + "   WHERE u.user_id = pa.changed_by)  AS changed_by,\n" + "  uuid,\n"
	        + "  '%s'                               AS facility,\n" + "  (CASE\n"
	        + "   WHEN pa.date_voided = GREATEST(COALESCE(pa.date_created, '1000-01-01'),\n"
	        + "                                 COALESCE(pa.date_changed, '1000-01-01'),\n"
	        + "                                 COALESCE(pa.date_voided, '1000-01-01'))\n" + "     THEN 'VOIDED'\n"
	        + "   WHEN pa.date_changed = GREATEST(COALESCE(pa.date_created, '1000-01-01'),\n"
	        + "                                  COALESCE(pa.date_changed, '1000-01-01'),\n"
	        + "                                  COALESCE(pa.date_voided, '1000-01-01'))\n" + "     THEN 'UPDATED'\n"
	        + "   ELSE 'NEW'\n" + "   END)                            AS state\n" + "FROM person_address pa\n"
	        + "WHERE pa.date_created > '%s' OR pa.date_changed > '%s' OR pa.date_voided > '%s'\n" + "LIMIT %s, %s";
	
	public static final String PERSON_ATTRIBUTE_QUERY = "SELECT\n" + "  (SELECT p.uuid\n" + "   FROM person AS p\n"
	        + "   WHERE p.person_id = pa.person_id)                                   AS person,\n" + "  value,\n"
	        + "  (SELECT pattr.uuid\n" + "   FROM person_attribute_type AS pattr\n"
	        + "   WHERE pa.person_attribute_type_id = pattr.person_attribute_type_id) AS person_attribute_type,\n"
	        + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = pa.creator)                                       AS creator,\n"
	        + "  pa.date_created,\n" + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = pa.changed_by)                                    AS changed_by,\n"
	        + "  pa.date_changed,\n" + "  pa.voided,\n" + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = pa.voided_by)                                     AS voided_by,\n"
	        + "  pa.date_voided,\n" + "  pa.void_reason,\n" + "  pa.uuid,\n"
	        + "  '%s'                                                                 AS facility,\n" + "  (CASE\n"
	        + "   WHEN pa.date_voided = GREATEST(COALESCE(pa.date_created, '1000-01-01'),\n"
	        + "                                 COALESCE(pa.date_changed, '1000-01-01'),\n"
	        + "                                 COALESCE(pa.date_voided, '1000-01-01'))\n" + "     THEN 'VOIDED'\n"
	        + "   WHEN pa.date_changed = GREATEST(COALESCE(pa.date_created, '1000-01-01'),\n"
	        + "                                  COALESCE(pa.date_changed, '1000-01-01'),\n"
	        + "                                  COALESCE(pa.date_voided, '1000-01-01'))\n" + "     THEN 'UPDATED'\n"
	        + "   ELSE 'NEW'\n" + "   END)                            AS state\n" + "FROM person_attribute pa\n"
	        + "WHERE pa.date_created > '%s' OR pa.date_changed > '%s' OR pa.date_voided > '%s'\n" + "LIMIT %s, %s";
	
	public static final String PERSON_NAME_QUERY = "SELECT\n" + "  preferred,\n" + "  (SELECT p.uuid\n"
	        + "   FROM person AS p\n" + "   WHERE p.person_id = pa.person_id) AS person,\n" + "  prefix,\n"
	        + "  given_name,\n" + "  middle_name,\n" + "  family_name_prefix,\n" + "  family_name,\n" + "  family_name2,\n"
	        + "  family_name_suffix,\n" + "  degree,\n" + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = pa.creator)     AS creator,\n" + "  date_created,\n" + "  voided,\n"
	        + "  (SELECT u.uuid\n" + "   FROM users AS u\n" + "   WHERE u.user_id = pa.voided_by)   AS voided_by,\n"
	        + "  date_voided,\n" + "  void_reason,\n" + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = pa.changed_by)  AS changed_by,\n" + "  date_changed,\n" + "  uuid,\n"
	        + "  '%s'                               AS facility,\n" + "  (CASE\n"
	        + "   WHEN pa.date_voided = GREATEST(COALESCE(pa.date_created, '1000-01-01'),\n"
	        + "                                 COALESCE(pa.date_changed, '1000-01-01'),\n"
	        + "                                 COALESCE(pa.date_voided, '1000-01-01'))\n" + "     THEN 'VOIDED'\n"
	        + "   WHEN pa.date_changed = GREATEST(COALESCE(pa.date_created, '1000-01-01'),\n"
	        + "                                  COALESCE(pa.date_changed, '1000-01-01'),\n"
	        + "                                  COALESCE(pa.date_voided, '1000-01-01'))\n" + "     THEN 'UPDATED'\n"
	        + "   ELSE 'NEW'\n" + "   END)                            AS state\n" + "FROM person_name pa\n"
	        + "WHERE pa.date_created > '%s' OR pa.date_changed > '%s' OR pa.date_voided > '%s'\n" + "LIMIT %s, %s";
	
	public static final String PATIENT_QUERY = "SELECT\n" + "  gender,\n" + "  birthdate,\n" + "  birthdate_estimated,\n"
	        + "  dead,\n" + "  death_date,\n" + "  cause_of_death,\n" + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = p.creator)    AS creator,\n" + "  p.date_created,\n" + "  (SELECT uuid\n"
	        + "   FROM users AS u\n" + "   WHERE u.user_id = p.changed_by) AS changed_by,\n" + "  p.date_changed,\n"
	        + "  p.voided,\n" + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = p.voided_by)  AS voided_by,\n" + "  p.date_voided,\n" + "  p.void_reason,\n"
	        + "  pp.uuid,\n" + "  '%s'                             AS facility,\n" + "  (CASE\n"
	        + "   WHEN p.date_voided = GREATEST(COALESCE(p.date_created, '1000-01-01'),\n"
	        + "                                 COALESCE(p.date_changed, '1000-01-01'),\n"
	        + "                                 COALESCE(p.date_voided, '1000-01-01'))\n" + "     THEN 'VOIDED'\n"
	        + "   WHEN p.date_changed = GREATEST(COALESCE(p.date_created, '1000-01-01'),\n"
	        + "                                  COALESCE(p.date_changed, '1000-01-01'),\n"
	        + "                                  COALESCE(p.date_voided, '1000-01-01'))\n" + "     THEN 'UPDATED'\n"
	        + "   ELSE 'NEW'\n" + "   END)                            AS state,\n" + "  deathdate_estimated,\n"
	        + "  birthtime,\n" + "  allergy_status\n"
	        + "FROM patient p INNER JOIN person pp ON (p.patient_id = pp.person_id)\n"
	        + "WHERE p.date_created > '%s' OR p.date_changed > '%s' OR p.date_voided > '%s'\n" + "LIMIT %s, %s";
	
	public static final String PATIENT_IDENTIFIER_QUERY = "SELECT\n" + "  (SELECT p.uuid\n" + "   FROM person AS p\n"
	        + "   WHERE p.person_id = pa.patient_id)                         AS patient,\n" + "  identifier,\n"
	        + "  (SELECT pit.uuid\n" + "   FROM patient_identifier_type AS pit\n"
	        + "   WHERE pit.patient_identifier_type_id = pa.identifier_type) AS identifier_type,\n" + "  preferred,\n"
	        + "  (SELECT uuid\n" + "   FROM location AS l\n"
	        + "   WHERE pa.location_id = l.location_id)                      AS location,\n" + "  (SELECT u.uuid\n"
	        + "   FROM users AS u\n" + "   WHERE u.user_id = pa.creator)                              AS creator,\n"
	        + "  date_created,\n" + "  date_changed,\n" + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = pa.changed_by)                           AS changed_by,\n" + "  voided,\n"
	        + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = pa.voided_by)                            AS voided_by,\n" + "  date_voided,\n"
	        + "  void_reason,\n" + "  uuid,\n"
	        + "  '%s'                                                        AS facility,\n" + "  (CASE\n"
	        + "   WHEN pa.date_voided = GREATEST(COALESCE(pa.date_created, '1000-01-01'),\n"
	        + "                                  COALESCE(pa.date_changed, '1000-01-01'),\n"
	        + "                                  COALESCE(pa.date_voided, '1000-01-01'))\n" + "     THEN 'VOIDED'\n"
	        + "   WHEN pa.date_changed = GREATEST(COALESCE(pa.date_created, '1000-01-01'),\n"
	        + "                                   COALESCE(pa.date_changed, '1000-01-01'),\n"
	        + "                                   COALESCE(pa.date_voided, '1000-01-01'))\n" + "     THEN 'UPDATED'\n"
	        + "   ELSE 'NEW'\n" + "   END)                                                       AS state\n"
	        + "FROM patient_identifier pa\n"
	        + "WHERE pa.date_created > '%s' OR pa.date_changed > '%s' OR pa.date_voided > '%s'\n" + "LIMIT %s, %s";
	
	public static final String VISIT_QUERY = "SELECT\n" + "  (SELECT p.uuid\n" + "   FROM person AS p\n"
	        + "   WHERE p.person_id = v.patient_id)             AS patient,\n" + "  (SELECT uuid\n"
	        + "   FROM visit_type AS vt\n" + "   WHERE vt.visit_type_id = v.visit_type_id)     AS visit_type,\n"
	        + "  date_started                                   AS start_datetime,\n"
	        + "  date_stopped                                   AS stop_datetime,\n" + "  (SELECT c.uuid\n"
	        + "   FROM concept AS c\n" + "   WHERE c.concept_id = v.indication_concept_id) AS indication_concept,\n"
	        + "  (SELECT l.uuid\n" + "   FROM location AS l\n"
	        + "   WHERE v.location_id = l.location_id)          AS location,\n" + "  (SELECT u.uuid\n"
	        + "   FROM users AS u\n" + "   WHERE u.user_id = v.creator)                  AS creator,\n"
	        + "  date_created,\n" + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = v.changed_by)               AS changed_by,\n" + "  date_changed,\n" + "  voided,\n"
	        + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = v.voided_by)                AS voided_by,\n" + "  date_voided,\n" + "  void_reason,\n"
	        + "  uuid,\n" + "  '%s'                                           AS facility,\n" + "  (CASE\n"
	        + "   WHEN v.date_voided = GREATEST(COALESCE(v.date_created, '1000-01-01'),\n"
	        + "                                 COALESCE(v.date_changed, '1000-01-01'),\n"
	        + "                                 COALESCE(v.date_voided, '1000-01-01'))\n" + "     THEN 'VOIDED'\n"
	        + "   WHEN v.date_changed = GREATEST(COALESCE(v.date_created, '1000-01-01'),\n"
	        + "                                  COALESCE(v.date_changed, '1000-01-01'),\n"
	        + "                                  COALESCE(v.date_voided, '1000-01-01'))\n" + "     THEN 'UPDATED'\n"
	        + "   ELSE 'NEW'\n" + "   END)                                          AS state\n" + "FROM visit v\n"
	        + "WHERE v.date_created > '%s' OR v.date_changed > '%s' OR v.date_voided > '%s'\n" + "LIMIT %s, %s";
	
	public static final String ENCOUNTER_QUERY = "SELECT\n" + "  (SELECT et.uuid\n" + "   FROM encounter_type AS et\n"
	        + "   WHERE et.encounter_type_id = e.encounter_type) AS encounter_type,\n" + "  (SELECT p.uuid\n"
	        + "   FROM person AS p\n" + "   WHERE p.person_id = e.patient_id)              AS patient,\n"
	        + "  (SELECT l.uuid\n" + "   FROM location AS l\n"
	        + "   WHERE e.location_id = l.location_id)           AS location,\n" + "  (SELECT uuid\n"
	        + "   FROM form AS f\n" + "   WHERE f.form_id = e.form_id)                   AS form,\n"
	        + "  encounter_datetime,\n" + "  (SELECT uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = e.creator)                   AS creator,\n" + "  date_created,\n" + "  voided,\n"
	        + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = e.voided_by)                 AS voided_by,\n" + "  date_voided,\n" + "  void_reason,\n"
	        + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = e.changed_by)                AS changed_by,\n" + "  date_changed,\n"
	        + "  (SELECT v.uuid\n" + "   FROM visit AS v\n"
	        + "   WHERE v.visit_id = e.visit_id)                 AS visit,\n" + "  uuid,\n"
	        + "  '%s'                                            AS facility,\n" + "  (CASE\n"
	        + "   WHEN e.date_voided = GREATEST(COALESCE(e.date_created, '1000-01-01'),\n"
	        + "                                 COALESCE(e.date_changed, '1000-01-01'),\n"
	        + "                                 COALESCE(e.date_voided, '1000-01-01'))\n" + "     THEN 'VOIDED'\n"
	        + "   WHEN e.date_changed = GREATEST(COALESCE(e.date_created, '1000-01-01'),\n"
	        + "                                  COALESCE(e.date_changed, '1000-01-01'),\n"
	        + "                                  COALESCE(e.date_voided, '1000-01-01'))\n" + "     THEN 'UPDATED'\n"
	        + "   ELSE 'NEW'\n" + "   END)                                           AS state\n" + "FROM encounter e\n"
	        + "WHERE e.date_created > '%s' OR e.date_changed > '%s' OR e.date_voided > '%s'\n" + "LIMIT %s, %s";
	
	public static final String OBS_QUERY = "SELECT\n" + "  (SELECT p.uuid\n" + "   FROM person AS p\n"
	        + "   WHERE p.person_id = o.person_id)                  AS person,\n" + "  (SELECT uuid\n"
	        + "   FROM concept AS c\n" + "   WHERE c.concept_id = o.concept_id)                AS concept,\n"
	        + "  (SELECT e.uuid\n" + "   FROM encounter AS e\n"
	        + "   WHERE e.encounter_id = o.encounter_id)            AS encounter,\n" + "  (SELECT oo.uuid\n"
	        + "   FROM `orders` AS oo\n" + "   WHERE oo.order_id = o.order_id)                   AS `order`,\n"
	        + "  obs_datetime,\n" + "  (SELECT l.uuid\n" + "   FROM location AS l\n"
	        + "   WHERE o.location_id = l.location_id)              AS location,\n" + "  (SELECT uuid\n"
	        + "   FROM obs AS oi\n" + "   WHERE oi.obs_id = o.obs_group_id)                 AS obs_group,\n"
	        + "  accession_number,\n" + "  value_group_id                                     AS value_group,\n"
	        + "  (SELECT uuid\n" + "   FROM concept AS c\n"
	        + "   WHERE c.concept_id = o.value_coded)               AS value_coded,\n" + "  (SELECT cn.uuid\n"
	        + "   FROM concept_name AS cn\n" + "   WHERE cn.concept_name_id = o.value_coded_name_id) AS value_coded_name,\n"
	        + "  (SELECT d.uuid\n" + "   FROM drug AS d\n"
	        + "   WHERE d.drug_id = o.value_drug)                   AS value_drug,\n" + "  value_datetime,\n"
	        + "  value_numeric,\n" + "  value_modifier,\n" + "  value_text,\n" + "  value_complex,\n" + "  comments,\n"
	        + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = o.creator)                      AS creator,\n" + "  date_created,\n" + "  voided,\n"
	        + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = o.voided_by)                    AS voided_by,\n" + "  date_voided,\n"
	        + "  void_reason,\n" + "  uuid,\n" + "  '%s'                                               AS facility,\n"
	        + "  (CASE\n" + "   WHEN o.date_voided IS NOT NULL\n" + "     THEN 'VOIDED'\n" + "   ELSE 'NEW'\n"
	        + "   END)                                              AS state,\n" + "  (SELECT oi.uuid\n"
	        + "   FROM obs AS oi\n" + "   WHERE oi.obs_id = o.previous_version)             AS previous_version\n"
	        + "FROM obs o\n" + "WHERE o.date_created > '%s' OR o.date_voided > '%s'\n" + "LIMIT %s, %s";
	
	public static final String ENCOUNTER_PROVIDER_QUERY = "SELECT\n" + "  (SELECT e.uuid\n" + "   FROM encounter AS e\n"
	        + "   WHERE e.encounter_id = ep.encounter_id)            AS encounter,\n" + "  (SELECT uuid\n"
	        + "   FROM provider AS p\n" + "   WHERE p.provider_id = ep.provider_id)              AS provider,\n"
	        + "  (SELECT er.uuid\n" + "   FROM encounter_role AS er\n"
	        + "   WHERE er.encounter_role_id = ep.encounter_role_id) AS encounter_role,\n" + "  (SELECT u.uuid\n"
	        + "   FROM users AS u\n" + "   WHERE u.user_id = ep.creator)                      AS creator,\n"
	        + "  date_created,\n" + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = ep.changed_by)                   AS changed_by,\n" + "  date_changed,\n" + "  voided,\n"
	        + "  date_voided,\n" + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = ep.voided_by)                    AS voided_by,\n" + "  void_reason,\n" + "  uuid,\n"
	        + "  '%s'                                                AS facility,\n" + "  (CASE\n"
	        + "   WHEN ep.date_voided = GREATEST(COALESCE(ep.date_created, '1000-01-01'),\n"
	        + "                                 COALESCE(ep.date_changed, '1000-01-01'),\n"
	        + "                                 COALESCE(ep.date_voided, '1000-01-01'))\n" + "     THEN 'VOIDED'\n"
	        + "   WHEN ep.date_changed = GREATEST(COALESCE(ep.date_created, '1000-01-01'),\n"
	        + "                                  COALESCE(ep.date_changed, '1000-01-01'),\n"
	        + "                                  COALESCE(ep.date_voided, '1000-01-01'))\n" + "     THEN 'UPDATED'\n"
	        + "   ELSE 'NEW'\n" + "   END)                            AS state\n" + "FROM encounter_provider ep\n"
	        + "WHERE ep.date_created > '%s' OR ep.date_changed > '%s' OR ep.date_voided > '%s'\n" + "LIMIT %s, %s";
	
	public static final String PROVIDER_QUERY = "SELECT\n" + "  (SELECT pp.uuid\n" + "   FROM person AS pp\n"
	        + "   WHERE pp.person_id = p.person_id)                AS person,\n" + "  name,\n" + "  identifier,\n"
	        + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = p.creator)                     AS creator,\n" + "  date_created,\n"
	        + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = p.changed_by)                  AS changed_by,\n" + "  date_changed,\n" + "  retired,\n"
	        + "  (SELECT u.uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = p.retired_by)                  AS retired_by,\n" + "  date_retired,\n"
	        + "  retire_reason,\n" + "  uuid,\n" + "  (SELECT ppr.uuid\n"
	        + "   FROM providermanagement_provider_role AS ppr\n"
	        + "   WHERE ppr.provider_role_id = p.provider_role_id) AS provider_role,\n"
	        + "  '%s'                                              AS facility,\n" + "  (CASE\n"
	        + "   WHEN p.date_retired = GREATEST(COALESCE(p.date_created, '1000-01-01'),\n"
	        + "                                 COALESCE(p.date_changed, '1000-01-01'),\n"
	        + "                                 COALESCE(p.date_retired, '1000-01-01'))\n" + "     THEN 'RETIRED'\n"
	        + "   WHEN p.date_changed = GREATEST(COALESCE(p.date_created, '1000-01-01'),\n"
	        + "                                  COALESCE(p.date_changed, '1000-01-01'),\n"
	        + "                                  COALESCE(p.date_retired, '1000-01-01'))\n" + "     THEN 'UPDATED'\n"
	        + "   ELSE 'NEW'\n" + "   END)                            AS state\n" + "FROM provider p\n"
	        + "WHERE p.date_created > '%s' OR p.date_changed > '%s' OR p.date_retired > '%s'\n" + "LIMIT %s, %s";
	
	public static final String ENCOUNTER_ROLE_QUERY = "SELECT\n" + "  name,\n" + "  description,\n" + "  (SELECT uuid\n"
	        + "   FROM users AS u\n" + "   WHERE u.user_id = er.creator)    AS creator,\n" + "  date_created,\n"
	        + "  (SELECT uuid\n" + "   FROM users AS u\n" + "   WHERE u.user_id = er.changed_by) AS changed_by,\n"
	        + "  date_changed,\n" + "  retired,\n" + "  (SELECT uuid\n" + "   FROM users AS u\n"
	        + "   WHERE u.user_id = er.retired_by) AS retired_by,\n" + "  date_retired,\n" + "  retire_reason,\n"
	        + "  uuid,\n" + "  '%s'                              AS facility,\n" + "  (CASE\n"
	        + "   WHEN er.date_retired = GREATEST(COALESCE(er.date_created, '1000-01-01'),\n"
	        + "                                  COALESCE(er.date_changed, '1000-01-01'),\n"
	        + "                                  COALESCE(er.date_retired, '1000-01-01'))\n" + "     THEN 'RETIRED'\n"
	        + "   WHEN er.date_changed = GREATEST(COALESCE(er.date_created, '1000-01-01'),\n"
	        + "                                  COALESCE(er.date_changed, '1000-01-01'),\n"
	        + "                                  COALESCE(er.date_retired, '1000-01-01'))\n" + "     THEN 'UPDATED'\n"
	        + "   ELSE 'NEW'\n" + "   END)                            AS state\n" + "FROM encounter_role er\n"
	        + "WHERE er.date_created > '%s' OR er.date_changed > '%s' OR er.date_retired > '%s'\n" + "LIMIT %s, %s";
	
	public static final String FINGERPRINT_QUERY = "SELECT\n" + "  patient,\n" + "  finger,\n" + "  fingerprint,\n"
	        + "  date_created,\n" + "  '%s'  AS facility,\n" + "  'NEW' AS state\n" + "FROM fingerprint\n"
	        + "WHERE date_created > '%s'\n" + "LIMIT %s, %s;";
	
	public static final String TABLES_TOTAL_QUERY = "SELECT\n"
	        + "  COUNT(encounter_id) AS number,\n"
	        + "  'encounter'         AS table_name\n"
	        + "FROM encounter WHERE date_created > 'lastSync' OR date_changed > 'lastSync' OR date_voided > 'lastSync'\n"
	        + "UNION ALL SELECT\n"
	        + "            COUNT(obs_id) AS number,\n"
	        + "            'obs'         AS table_name\n"
	        + "          FROM obs WHERE date_created > 'lastSync' OR date_voided > 'lastSync'\n"
	        + "UNION ALL SELECT\n"
	        + "            COUNT(person_id) AS number,\n"
	        + "            'person'         AS table_name\n"
	        + "          FROM person WHERE date_created > 'lastSync' OR date_changed > 'lastSync' OR date_voided > 'lastSync'\n"
	        + "UNION ALL\n"
	        + "SELECT\n"
	        + "  COUNT(person_name_id) AS number,\n"
	        + "  'person_name'         AS table_name\n"
	        + "FROM person_name WHERE date_created > 'lastSync' OR date_changed > 'lastSync' OR date_voided > 'lastSync'\n"
	        + "UNION ALL SELECT\n"
	        + "            COUNT(person_address_id) AS number,\n"
	        + "            'person_address'         AS table_name\n"
	        + "          FROM person_address WHERE date_created > 'lastSync' OR date_changed > 'lastSync' OR date_voided > 'lastSync'\n"
	        + "UNION ALL SELECT\n"
	        + "            COUNT(person_attribute_type_id) AS number,\n"
	        + "            'person_attribute'              AS table_name\n"
	        + "          FROM person_attribute WHERE date_created > 'lastSync' OR date_changed > 'lastSync' OR date_voided > 'lastSync'\n"
	        + "UNION ALL SELECT\n"
	        + "            COUNT(patient_id) AS number,\n"
	        + "            'patient'         AS table_name\n"
	        + "          FROM patient WHERE date_created > 'lastSync' OR date_changed > 'lastSync' OR date_voided > 'lastSync'\n"
	        + "UNION ALL SELECT\n"
	        + "            COUNT(patient_identifier_id) AS number,\n"
	        + "            'patient_identifier'         AS table_name\n"
	        + "          FROM patient_identifier WHERE date_created > 'lastSync' OR date_changed > 'lastSync' OR date_voided > 'lastSync'\n"
	        + "UNION ALL SELECT\n"
	        + "            COUNT(visit_id) AS number,\n"
	        + "            'visit'         AS table_name\n"
	        + "          FROM visit WHERE date_created > 'lastSync' OR date_changed > 'lastSync' OR date_voided > 'lastSync'\n"
	        + "UNION ALL SELECT\n"
	        + "            COUNT(encounter_provider_id) AS number,\n"
	        + "            'encounter_provider'         AS table_name\n"
	        + "          FROM encounter_provider WHERE date_created > 'lastSync' OR date_changed > 'lastSync' OR date_voided > 'lastSync'\n"
	        + "UNION ALL SELECT\n"
	        + "            COUNT(provider_id) AS number,\n"
	        + "            'provider'         AS table_name\n"
	        + "          FROM provider WHERE date_created > 'lastSync' OR date_changed > 'lastSync' OR date_retired > 'lastSync'\n"
	        + "UNION ALL SELECT\n"
	        + "            COUNT(encounter_role_id) AS number,\n"
	        + "            'encounter_role'         AS table_name\n"
	        + "          FROM encounter_role WHERE date_created > 'lastSync' OR date_changed > 'lastSync' OR date_retired > 'lastSync'\n"
	        + "UNION ALL SELECT\n" + "            COUNT(fingerprint_id) AS number,\n"
	        + "            'fingerprint'         AS table_name\n"
	        + "          FROM fingerprint WHERE date_created > 'lastSync'";
	
	public static List<String> ENCOUNTER_COLUMNS = Arrays.asList("encounter_type", "patient", "location", "form",
	    "encounter_datetime", "creator", "date_created", "voided", "voided_by", "date_voided", "void_reason", "changed_by",
	    "date_changed", "visit", "uuid", "facility", "state");
	
	public static List<String> PERSON_COLUMNS = Arrays.asList("gender", "birthdate", "birthdate_estimated", "dead",
	    "death_date", "cause_of_death", "creator", "date_created", "changed_by", "date_changed", "voided", "voided_by",
	    "date_voided", "void_reason", "uuid", "facility", "state", "deathdate_estimated", "birthtime");
	
	public static List<String> PERSON_ADDRESS_COLUMNS = Arrays.asList("person", "preferred", "address1", "address2",
	    "city_village", "state_province", "postal_code", "country", "latitude", "longitude", "start_date", "end_date",
	    "creator", "date_created", "voided", "voided_by", "date_voided", "void_reason", "county_district", "address3",
	    "address4", "address5", "address6", "date_changed", "changed_by", "uuid", "facility", "state");
	
	public static List<String> PERSON_ATTRIBUTE_COLUMNS = Arrays.asList("person", "value", "person_attribute_type",
	    "creator", "date_created", "changed_by", "date_changed", "voided", "voided_by", "date_voided", "void_reason",
	    "uuid", "facility", "state");
	
	public static List<String> PERSON_NAME_COLUMNS = Arrays.asList("preferred", "person", "prefix", "given_name",
	    "middle_name", "family_name_prefix", "family_name", "family_name2", "family_name_suffix", "degree", "creator",
	    "date_created", "voided", "voided_by", "date_voided", "void_reason", "changed_by", "date_changed", "uuid",
	    "facility", "state");
	
	public static List<String> PATIENT_COLUMNS = Arrays.asList("gender", "birthdate", "birthdate_estimated", "dead",
	    "death_date", "cause_of_death", "creator", "date_created", "changed_by", "date_changed", "voided", "voided_by",
	    "date_voided", "void_reason", "uuid", "facility", "state", "deathdate_estimated", "birthtime", "allergy_status");
	
	public static List<String> PATIENT_IDENTIFIER_COLUMNS = Arrays.asList("patient", "identifier", "identifier_type",
	    "preferred", "location", "creator", "date_created", "date_changed", "changed_by", "voided", "voided_by",
	    "date_voided", "void_reason", "uuid", "facility", "state");
	
	public static List<String> VISIT_COLUMNS = Arrays.asList("patient", "visit_type", "start_datetime", "stop_datetime",
	    "indication_concept", "location", "creator", "date_created", "changed_by", "date_changed", "voided", "voided_by",
	    "date_voided", "void_reason", "uuid", "facility", "state");
	
	public static List<String> OBS_COLUMNS = Arrays
	        .asList("person", "concept", "encounter", "`order`", "obs_datetime", "location", "obs_group",
	            "accession_number", "value_group", "value_coded", "value_coded_name", "value_drug", "value_datetime",
	            "value_numeric", "value_modifier", "value_text", "value_complex", "comments", "creator", "date_created",
	            "voided", "voided_by", "date_voided", "void_reason", "uuid", "facility", "state", "previous_version");
	
	public static List<String> ENCOUNTER_PROVIDER_COLUMNS = Arrays.asList("encounter", "provider", "encounter_role",
	    "creator", "date_created", "changed_by", "date_changed", "voided", "date_voided", "voided_by", "void_reason",
	    "uuid", "facility", "state");
	
	public static List<String> PROVIDER_COLUMNS = Arrays.asList("person", "name", "identifier", "creator", "date_created",
	    "changed_by", "date_changed", "retired", "retired_by", "date_retired", "retire_reason", "uuid", "provider_role",
	    "facility", "state");
	
	public static List<String> FINGERPRINT_COLUMNS = Arrays.asList("patient", "finger", "fingerprint", "date_created",
	    "facility", "state");

    public static final String VIRAL_LOAD_ENCOUNTER_QUERY = "select * from encounter inner join encounter_type on (encounter.encounter_type=encounter_type.encounter_type_id) where encounter_type.uuid='077c43ee-9745-11e9-bc42-526af7764f64'";

    public static final String VIRAL_LOAD_ORDERS_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) inner JOIN concept ON (orders.concept_id = concept.concept_id) inner join concept_reference_map on (concept.concept_id = concept_reference_map.concept_id) inner join concept_reference_term on (concept_reference_map.concept_reference_term_id = concept_reference_term.concept_reference_term_id) where accession_number!=\"\" AND specimen_source!=\"\" AND orders.instructions=\"REFER TO cphl\" AND code=\"" + VIRAL_LOAD_SYNC_TASK_TYPE_IDENTIFIER + "\"";

    public static final String VIRAL_LOAD_ORDER_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) where accession_number=\"%s\"";

    public static final String PERSON_UUID_QUERY="select uuid from person WHERE date_created > '%s' OR date_changed > '%s' OR date_voided > '%s'";
	public static final String PATIENT_UUID_QUERY="select uuid from patient inner join person on (person.person_id =patient.patient_id) WHERE patient.date_created > '%s' OR patient.date_changed > '%s' OR patient.voided > '%s'";
    public static final String ENCOUNTER_UUID_QUERY="select uuid from encounter WHERE date_created > '%s' OR date_changed > '%s' OR date_voided > '%s'";
    public static final String OBSERVATION_UUID_QUERY="select uuid from obs WHERE date_created > '%s' OR date_voided > '%s'";
    public static final String PRACTITIONER_UUID_QUERY="select uuid from provider WHERE date_created > '%s' OR date_changed > '%s' OR date_retired > '%s'";

    public static final String FHIR_BUNDLE_RESOURCE_FOR_COLLECTION="{\"resourceType\": \"Bundle\",\"type\": \"collection\",\"entry\": [%s]}";


    public static List<String> VL_SYNC_TASK_COLUMNS = Arrays.asList("sync_task_id", "sync_task_type", "sync_task", "status", "status_code", "sent_to_url", "require_action", "action_completed", "date_sent", "creator", "date_created", "changed_by", "date_changed", "voided", "date_voided", "voided_by ", "void_reason", "uuid");
}