/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.model.SyncFhirCase;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfileLog;
import org.openmrs.module.ugandaemrsync.model.SyncFhirResource;
import org.openmrs.module.ugandaemrsync.server.SyncConstant;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.FHIR_FILTER_OBJECT_STRING;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VIRAL_LOAD_SYNC_TYPE_UUID;

/**
 * This is a unit test, which verifies logic in UgandaEMRSyncService. It doesn't extend
 * BaseModuleContextSensitiveTest, thus it is run without the in-memory DB and Spring context.
 */
public class UgandaEMRSyncServiceTest extends BaseModuleContextSensitiveTest {
    protected static final String UGANDAEMRSYNC_GLOBALPROPERTY_DATASET_XML = "org/openmrs/module/ugandaemrsync/include/globalPropertiesDataSet.xml";
    protected static final String UGANDAEMRSYNC_STANDARDTESTDATA = "org/openmrs/module/ugandaemrsync/include/standardTestDataset.xml";

    private static final String sampleResultsForCd4Test = "{\"resourceType\":\"Bundle\",\"type\":\"transaction-response\",\"entry\":[{\"fullUrl\":\"urn:uuid:158aba56-c34a-4df8-8c00-b7b496575553\",\"resource\":{\"resourceType\":\"DiagnosticReport\",\"basedOn\":[{\"reference\":\"ServiceRequest/123455\"}],\"subject\":{\"reference\":\"urn:uuid:da3c5276-a48e-48b0-9f37-53d4c2d496f2\"},\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://SSTRM.org\",\"code\":\"CD41003\",\"display\":\"CD4 COunt\"}],\"text\":\"CD4 COunt\"}],\"code\":{\"coding\":[{\"system\":\"http://SSTRM.org\",\"code\":\"CD41003\",\"display\":\"CD4 COunt\"}],\"text\":\"CD4 COunt\"},\"effectivePeriod\":{\"start\":\"2021-06-23T11:45:33+11:00\",\"end\":\"2021-06-23T11:45:33+11:00\"},\"issued\":\"2021-06-23T11:45:33+11:00\",\"performer\":[{\"reference\":\"urn:uuid:652d2dad-ae63-4d52-9984-1abdae5f3959\",\"type\":\"Practitioner\",\"display\":\"performed_by\"},{\"reference\":\"urn:uuid:fac46899-329d-42b1-9d30-238bf5a53f6a\",\"type\":\"Practitioner\",\"display\":\"reviewed_by\"},{\"reference\":\"urn:uuid:016477c1-3fe5-4c90-9482-3b745fde744d\",\"type\":\"Practitioner\",\"display\":\"authorised_by\"}],\"specimen\":[{\"reference\":\"urn:uuid:63510ef5-2e99-430b-8fee-1cfe9ea86496\"}],\"result\":[{\"reference\":\"Observation/observation-24\"}]}},{\"fullUrl\":\"urn:uuid:ac13f104-648f-45a7-8c88-9ae6e713b8d4\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-24\",\"basedOn\":[{\"reference\":\"ServiceRequest/123455\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://SSTRM.org\",\"code\":\"CD41003\",\"display\":\"CD4 COunt\"}],\"text\":\"CD4 COunt\"},\"valueQuantity\":{\"value\":400.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"referenceRange\":[{\"low\":{\"value\":7.0,\"unit\":\"ml\",\"system\":\"http://unitsofmeasure.org\"},\"high\":{\"value\":1000,\"unit\":\"ml\",\"system\":\"http://unitsofmeasure.org\"}}],\"device\":{\"reference\":\"#device-1\"}}}]}";
    private static final String sampleResultsForCBCTest = "{\"resourceType\":\"Bundle\",\"type\":\"transaction-response\",\"entry\":[{\"fullUrl\":\"urn:uuid:158aba56-c34a-4df8-8c00-b7b496575553\",\"resource\":{\"resourceType\":\"DiagnosticReport\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"subject\":{\"reference\":\"urn:uuid:da3c5276-a48e-48b0-9f37-53d4c2d496f2\"},\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"252275004\",\"display\":\"Hematology test (procedure)\"}],\"text\":\"Hematology test\"}],\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"26604007\",\"display\":\"Complete blood count (procedure)    \"}],\"text\":\"Complete Blood Count(CBC)\"},\"effectivePeriod\":{\"start\":\"2021-06-23T11:45:33+11:00\",\"end\":\"2021-06-23T11:45:33+11:00\"},\"issued\":\"2021-06-23T11:45:33+11:00\",\"performer\":[{\"reference\":\"urn:uuid:652d2dad-ae63-4d52-9984-1abdae5f3959\",\"type\":\"Practitioner\",\"display\":\"performed_by\"},{\"reference\":\"urn:uuid:fac46899-329d-42b1-9d30-238bf5a53f6a\",\"type\":\"Practitioner\",\"display\":\"reviewed_by\"},{\"reference\":\"urn:uuid:016477c1-3fe5-4c90-9482-3b745fde744d\",\"type\":\"Practitioner\",\"display\":\"authorised_by\"}],\"specimen\":[{\"reference\":\"urn:uuid:63510ef5-2e99-430b-8fee-1cfe9ea86496\"}],\"result\":[{\"reference\":\"Observation/p1\"}]}},{\"fullUrl\":\"https://example.com/base/Observation/p1\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"p1\",\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"26604007\",\"display\":\"Complete blood count (procedure)    \"}],\"text\":\"Complete Blood Count(CBC)\"},\"effectiveDateTime\":\"2015-08-16T06:40:17Z\",\"issued\":\"2015-08-17T06:40:17Z\",\"performer\":[{\"reference\":\"Organization/1832473e-2fe0-452d-abe9-3cdb9879522f\",\"display\":\"Acme Laboratory, Inc\"}],\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"hasMember\":[{\"reference\":\"Observation/observation-1\"},{\"reference\":\"Observation/observation-2\"},{\"reference\":\"Observation/observation-3\"},{\"reference\":\"Observation/observation-4\"},{\"reference\":\"Observation/observation-5\"},{\"reference\":\"Observation/observation-6\"},{\"reference\":\"Observation/observation-7\"},{\"reference\":\"Observation/observation-8\"},{\"reference\":\"Observation/observation-9\"},{\"reference\":\"Observation/observation-10\"},{\"reference\":\"Observation/observation-11\"},{\"reference\":\"Observation/observation-12\"},{\"reference\":\"Observation/observation-13\"},{\"reference\":\"Observation/observation-14\"},{\"reference\":\"Observation/observation-15\"},{\"reference\":\"Observation/observation-16\"},{\"reference\":\"Observation/observation-17\"},{\"reference\":\"Observation/observation-18\"},{\"reference\":\"Observation/observation-19\"},{\"reference\":\"Observation/observation-20\"},{\"reference\":\"Observation/observation-21\"},{\"reference\":\"Observation/observation-22\"},{\"reference\":\"Observation/observation-23\"},{\"reference\":\"Observation/observation-24\"}]}},{\"fullUrl\":\"urn:uuid:63510ef5-2e99-430b-8fee-1cfe9ea86496\",\"resource\":{\"resourceType\":\"Specimen\",\"accessionIdentifier\":{\"value\":\"NL/009/072021\"},\"type\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"119297000\",\"display\":\"Blood specimen (specimen)\"}],\"text\":\"Blood\"},\"collection\":{\"collector\":{\"reference\":\"urn:uuid:a9dbdbc8-12f5-4377-97b8-0abb7c4b3c2b\",\"type\":\"Practitioner\",\"display\":\"sample_obtainer\"},\"collectedPeriod\":{\"start\":\"2021-05-06T11:45:33+11:00\",\"end\":\"2021-05-06T11:45:33+11:00\"}}}},{\"fullUrl\":\"urn:uuid:3164847e-fe56-4bc8-adea-8d8706534f6c\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-1\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"767002\",\"display\":\"White blood cell count (procedure)\"}],\"text\":\"WBC\"},\"valueQuantity\":{\"value\":7.26,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9g/L\"},\"referenceRange\":[{\"low\":{\"value\":4.00,\"unit\":\"x10*9g/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9g/L\"},\"high\":{\"value\":10.00,\"unit\":\"x10*9g/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9g/L\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:1aac0390-6836-42d1-b7cc-c09c53a6db06\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-2\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"271035003\",\"display\":\"Neutrophil percent differential count (procedure)\"}],\"text\":\"Neut%\"},\"valueQuantity\":{\"value\":50.6,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"referenceRange\":[{\"low\":{\"value\":50.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"high\":{\"value\":70.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:75d1f0a3-6492-4689-8ad3-055f51f06e9a\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-3\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"271036002\",\"display\":\"Lymphocyte percent differential count (procedure)\"}],\"text\":\"Lymph%\"},\"valueQuantity\":{\"value\":38.7,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"referenceRange\":[{\"low\":{\"value\":20.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"high\":{\"value\":40.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:4df1a952-26f3-451e-85bd-65cf7ece0869\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-4\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"271037006\",\"display\":\"Monocyte percent differential count (procedure)\"}],\"text\":\"Mono%\"},\"valueQuantity\":{\"value\":8.2,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"referenceRange\":[{\"low\":{\"value\":3.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"high\":{\"value\":12.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:f830fc55-6cdc-495a-8983-4beeabf34ccf\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-5\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"310540006\",\"display\":\"Eosinophil percent differential count (procedure)\"}],\"text\":\"Eo%\"},\"valueQuantity\":{\"value\":2.3,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"referenceRange\":[{\"low\":{\"value\":0.5,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"high\":{\"value\":5.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:43462716-382c-4712-8583-d06c41e98549\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-6\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"271038001\",\"display\":\"Basophil percent differential count (procedure) \"}],\"text\":\"Baso%\"},\"valueQuantity\":{\"value\":0.2,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"referenceRange\":[{\"low\":{\"value\":0.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"high\":{\"value\":1.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:c6587780-f1c5-4517-98ac-bbff25d0e97f\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-7\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"30630007\",\"display\":\"Neutrophil count (procedure)\"}],\"text\":\"Neut#\"},\"valueQuantity\":{\"value\":3.67,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"referenceRange\":[{\"low\":{\"value\":2.00,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"high\":{\"value\":7.00,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:4f4f831f-5788-4beb-8afa-5648a2ee5ef3\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-8\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"74765001\",\"display\":\"Lymphocyte count (procedure)\"}],\"text\":\"Lymph#\"},\"valueQuantity\":{\"value\":2.81,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"referenceRange\":[{\"low\":{\"value\":0.80,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"high\":{\"value\":4.00,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:ddcc2ccc-de92-40df-899d-44639632e399\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-9\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"67776007\",\"display\":\"Monocyte count (procedure)\"}],\"text\":\"Mono#\"},\"valueQuantity\":{\"value\":0.60,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"referenceRange\":[{\"low\":{\"value\":0.12,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"high\":{\"value\":1.20,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:bfac1e99-e389-4fbd-baaf-d238c63ed6ba\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-10\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"71960002\",\"display\":\"Eosinophil count (procedure) \"}],\"text\":\"Eo#\"},\"valueQuantity\":{\"value\":0.17,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"referenceRange\":[{\"low\":{\"value\":0.02,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"high\":{\"value\":0.50,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:b8fd3518-14c9-415a-8582-154646b2cd65\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-11\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"42351005\",\"display\":\"Basophil count (procedure)\"}],\"text\":\"Baso#\"},\"valueQuantity\":{\"value\":0.01,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"referenceRange\":[{\"low\":{\"value\":0.00,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"high\":{\"value\":0.10,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:499a6169-0449-4b3a-ac48-4f150e689913\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-12\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"52641009\",\"display\":\"Atypical lymphocyte (morphologic abnormality)\"}],\"text\":\"*ALY#\"},\"valueQuantity\":{\"value\":0.01,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"referenceRange\":[{\"low\":{\"value\":0.00,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"high\":{\"value\":0.20,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:afa0bef6-25e2-4d6d-9543-4f8c293301cb\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-13\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"14089001\",\"display\":\"Red blood cell count (procedure)\"}],\"text\":\"RBC\"},\"valueQuantity\":{\"value\":5.70,\"unit\":\"x10*12/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*12/L\"},\"referenceRange\":[{\"low\":{\"value\":4.00,\"unit\":\"x10*12/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*12/L\"},\"high\":{\"value\":5.50,\"unit\":\"x10*12/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*12/L\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:74a6a832-8074-4d75-b918-f05cc7ca3530\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-14\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"38082009\",\"display\":\"Hemoglobin (substance) \"}],\"text\":\"HGB\"},\"valueQuantity\":{\"value\":14.9,\"unit\":\"g/dL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"g/dL\"},\"referenceRange\":[{\"low\":{\"value\":12.0,\"unit\":\"g/dL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"g/dL\"},\"high\":{\"value\":16.0,\"unit\":\"g/dL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"g/dL\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:2a24c8b8-2f4a-4417-92b9-f3b36737c121\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-15\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"165455001\",\"display\":\"Mean corpuscular volume - low (finding) \"}],\"text\":\"HCT\"},\"valueQuantity\":{\"value\":45.5,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"referenceRange\":[{\"low\":{\"value\":40.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"high\":{\"value\":54.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:fdbd3af9-cc30-41c1-8de7-abf363ab7329\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-16\",\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"165455001\",\"display\":\"Mean corpuscular volume - low (finding)\"}],\"text\":\"MCV\"},\"valueQuantity\":{\"value\":79.7,\"unit\":\"fL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"fL\"},\"referenceRange\":[{\"low\":{\"value\":80.0,\"unit\":\"fL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"fL\"},\"high\":{\"value\":100.0,\"unit\":\"fL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"fL\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:bb786024-f7e3-4161-845b-5adc6daa23e0\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-17\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"165439008\",\"display\":\"Mean corpuscular hemoglobin low (finding)\"}],\"text\":\"MCH\"},\"valueQuantity\":{\"value\":26.2,\"unit\":\"pg\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"pg\"},\"referenceRange\":[{\"low\":{\"value\":27.0,\"unit\":\"pg\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"pg\"},\"high\":{\"value\":34.0,\"unit\":\"pg\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"pg\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:a8afcd29-43a0-489a-9696-9e669e64d0b1\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-18\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"165439008\",\"display\":\"Mean corpuscular hemoglobin low (finding)\"}],\"text\":\"MCHC\"},\"valueQuantity\":{\"value\":32.8,\"unit\":\"g/dL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"g/dL\"},\"referenceRange\":[{\"low\":{\"value\":32.0,\"unit\":\"g/dL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"g/dL\"},\"high\":{\"value\":36.0,\"unit\":\"g/dL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"g/dL\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:3ff90d3f-131c-49b3-9ecc-58ca40b73bae\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-19\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://loinc.org\",\"code\":\"788-0\",\"display\":\"Erythrocyte distribution width [Ratio] by Automated count\"}],\"text\":\"RDW-CV\"},\"valueQuantity\":{\"value\":19.2,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"%\"},\"referenceRange\":[{\"low\":{\"value\":11.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"%\"},\"high\":{\"value\":16.0,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"%\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:c3ba82a8-f6c4-4757-a4f9-224e70deaae8\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-20\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://loinc.org\",\"code\":\"21000-5\",\"display\":\"Erythrocyte distribution width [Entitic volume] by Automated count\"}],\"text\":\"RDW-SD\"},\"valueQuantity\":{\"value\":44.3,\"unit\":\"fL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"fL\"},\"referenceRange\":[{\"low\":{\"value\":37.0,\"unit\":\"fL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"fL\"},\"high\":{\"value\":54.0,\"unit\":\"fL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"fL\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:f1a0bb1a-5d40-4262-9b11-0154837b3dec\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-21\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"365632008\",\"display\":\"Finding of platelet count (finding)\"}],\"text\":\"PLT\"},\"valueQuantity\":{\"value\":253,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"referenceRange\":[{\"low\":{\"value\":100,\"unit\":\"10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"},\"high\":{\"value\":300,\"unit\":\"x10*9/L\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"10*9/L\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:d0b33f89-e0f8-4fa8-9bec-52deafeb67e4\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-22\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"75672003\",\"display\":\"Platelet mean volume determination (procedure)\"}],\"text\":\"MPV\"},\"valueQuantity\":{\"value\":8.6,\"unit\":\"fL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"fL\"},\"referenceRange\":[{\"low\":{\"value\":6.5,\"unit\":\"fL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"fL\"},\"high\":{\"value\":12.0,\"unit\":\"fL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"fL\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:d67f375d-fd31-41e9-a7cc-7af293528f48\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-23\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"250313005\",\"display\":\"Platelet distribution width measurement (procedure)\"}],\"text\":\"PDW\"},\"valueQuantity\":{\"value\":11.1,\"unit\":\"fL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"fL\"},\"referenceRange\":[{\"low\":{\"value\":9.0,\"unit\":\"fL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"fL\"},\"high\":{\"value\":17.0,\"unit\":\"fL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"fL\"}}],\"device\":{\"reference\":\"urn:uuid:8f6a8d79-87fa-4db3-9c05-164c2908ead6\"}}},{\"fullUrl\":\"urn:uuid:ac13f104-648f-45a7-8c88-9ae6e713b8d4\",\"resource\":{\"resourceType\":\"Observation\",\"id\":\"observation-24\",\"basedOn\":[{\"reference\":\"ServiceRequest/04c1c2d7-d496-449f-b502-889507a1fb8d\"}],\"status\":\"final\",\"code\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"250314004\",\"display\":\"Platelet hematocrit measurement (procedure)\"}],\"text\":\"PCT\"},\"valueQuantity\":{\"value\":0.217,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"referenceRange\":[{\"low\":{\"value\":0.108,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"},\"high\":{\"value\":0.282,\"unit\":\"%\",\"system\":\"http://unitsofmeasure.org\"}}],\"device\":{\"reference\":\"#device-1\"}}}]}";
    private static final Integer cd4Concept = 5497;
    private static final Integer WBCCountConcept = 678;
    private UgandaEMRSyncService ugandaEMRSyncService;
    private int currentYearSuffix;

    @Before
    public void initialize() throws Exception {
        executeDataSet(UGANDAEMRSYNC_GLOBALPROPERTY_DATASET_XML);
        executeDataSet(UGANDAEMRSYNC_STANDARDTESTDATA);
    }

    @Before
    public void setUp() {
        ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        currentYearSuffix = Year.now().getValue() % 100;
    }

    @Before
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFacilityConcatenation() {

        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        syncGlobalProperties.setSyncFacilityProperties();
        String facilityId = syncGlobalProperties.getGlobalProperty(SyncConstant.HEALTH_CENTER_SYNC_ID);

        String query = "SELECT\n" + "  name,\n" + "  description,\n" + "  (SELECT uuid\n" + "   FROM users AS u\n" + "   WHERE u.user_id = er.creator)    AS creator,\n" + "  date_created,\n" + "  (SELECT uuid\n" + "   FROM users AS u\n" + "   WHERE u.user_id = er.changed_by) AS changed_by,\n" + "  date_changed,\n" + "  retired,\n" + "  (SELECT uuid\n" + "   FROM users AS u\n" + "   WHERE u.user_id = er.retired_by) AS retired_by,\n" + "  date_retired,\n" + "  retire_reason,\n" + "  uuid,\n" + String.format("  '%s'                        AS facility,\n", facilityId) + "  'NEW'                             AS state\n" + "FROM encounter_role er";

        // assertNotNull(facilityId);
        //assertTrue(query.contains(facilityId));
    }

    @Test
    public void saveSyncTaskType_shouldSaveSyncTaskType() throws Exception {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        List<SyncTaskType> syncTaskTypesBeforeSavingingMore = ugandaEMRSyncService.getAllSyncTaskType();
        SyncTaskType neSyncTaskType = new SyncTaskType();
        neSyncTaskType.setDateCreated(new Date());
        neSyncTaskType.setName("SyncTaskType1");
        neSyncTaskType.setDataType("org.openmrs.Concepts");
        neSyncTaskType.setUrl("http://google.com");
        neSyncTaskType.setUrlUserName("samuel");
        neSyncTaskType.setUrlPassword("samule");
        neSyncTaskType.setUrlToken("agehgyryteghuteded");
        neSyncTaskType.setDataTypeId("4672");
        neSyncTaskType.setCreator(Context.getAuthenticatedUser());
        ugandaEMRSyncService.saveSyncTaskType(neSyncTaskType);

        List<SyncTaskType> syncTaskTypes = ugandaEMRSyncService.getAllSyncTaskType();

        Assert.assertEquals(syncTaskTypesBeforeSavingingMore.size() + 1, syncTaskTypes.size());
    }

    @Test
    public void saveSyncTask_shouldSaveSyncTask() throws Exception {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID);
        SyncTask newSyncTask = new SyncTask();
        newSyncTask.setDateSent(new Date());
        newSyncTask.setCreator(Context.getUserService().getUser(1));
        newSyncTask.setSentToUrl(syncTaskType.getUrl());
        newSyncTask.setRequireAction(true);
        newSyncTask.setActionCompleted(false);
        newSyncTask.setSyncTask("1234");
        newSyncTask.setStatusCode(200);
        newSyncTask.setStatus("SUCCESS");
        newSyncTask.setSyncTaskType(ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID));
        ugandaEMRSyncService.saveSyncTask(newSyncTask);
        List<SyncTask> syncTasks = ugandaEMRSyncService.getAllSyncTask();

        Assert.assertEquals(2, syncTasks.size());
    }


    @Test
    public void getAllSyncTask_ShouldReturnAllsyncTaskTypes() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        List<SyncTaskType> syncTaskTypes = ugandaEMRSyncService.getAllSyncTaskType();

        Assert.assertEquals(2, syncTaskTypes.size());
    }

    @Before
    public void initializeSyncTask() throws Exception {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID);
        SyncTask newSyncTask = new SyncTask();
        newSyncTask.setDateSent(new Date());
        newSyncTask.setCreator(Context.getUserService().getUser(1));
        newSyncTask.setSentToUrl(syncTaskType.getUrl());
        newSyncTask.setRequireAction(true);
        newSyncTask.setActionCompleted(false);
        newSyncTask.setSyncTask("1234");
        newSyncTask.setStatusCode(200);
        newSyncTask.setStatus("SUCCESS");
        newSyncTask.setSyncTaskType(ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID));
        ugandaEMRSyncService.saveSyncTask(newSyncTask);
    }

    @Test
    public void getAllSyncTask_ShouldReturnAllSyncTask() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        List<SyncTask> syncTask = ugandaEMRSyncService.getAllSyncTask();

        Assert.assertEquals(1, syncTask.size());
    }

    @Test
    public void getSyncTaskBySyncTaskId_ShouldReturnSyncTask() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncTask syncTask = ugandaEMRSyncService.getSyncTaskBySyncTaskId("1234");

        Assert.assertEquals("1234", syncTask.getSyncTask());
    }


    @Test
    public void getSyncTaskTypeByUUID_shouldReturnSyncTaskTypeThatMatchesUUID() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID);

        Assert.assertEquals(VIRAL_LOAD_SYNC_TYPE_UUID, syncTaskType.getUuid());
    }

    @Test
    public void getSyncT_shouldReturnSyncTaskTypeThatMatchesUUID() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID);
        List<SyncTask> syncTasks = ugandaEMRSyncService.getIncompleteActionSyncTask(syncTaskType.getUuid());

        Assert.assertNotEquals(0, syncTasks.size());
        Assert.assertEquals(VIRAL_LOAD_SYNC_TYPE_UUID, syncTasks.get(0).getSyncTaskType().getUuid());
        Assert.assertEquals(false, syncTasks.get(0).getActionCompleted());
        Assert.assertEquals(true, syncTasks.get(0).getRequireAction());
    }

    @Test
    public void convertStringToDate_shouldReturnDate() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        Assert.assertNotNull(ugandaEMRSyncService.convertStringToDate("2013-08-02", "00:00:00", "yyyy-MM-dd"));
    }

    @Test
    public void getDateFormat_shouldGetDateFormatFromGivenDate() {
        Assert.assertEquals("yyyy-MM-dd", Context.getService(UgandaEMRSyncService.class).getDateFormat("2013-08-02"));
    }

    @Test
    public void getPatientIdentifier_shouldGetDateFormatFromGivenDate() {
        Patient patient = Context.getService(UgandaEMRSyncService.class).getPatientByPatientIdentifier("101-6");
        Assert.assertNotNull(patient);
        Assert.assertEquals("101-6", patient.getPatientIdentifier().getIdentifier());
    }

    @Test
    public void validateFacility_shouldReturnTrueWhenStringIsFacilityDHIS2UUID() {
        Assert.assertTrue(Context.getService(UgandaEMRSyncService.class).validateFacility("7744yxP"));
    }

    @Test
    public void addVLToEncounter_shouldSaveViralLoadResultToSelectedEncounter() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        Encounter encounter = Context.getEncounterService().getEncounter(1000);
        Order order = Context.getOrderService().getOrderByUuid("b3230032-00c4-11ef-bce1-4725e612a60b");
        ugandaEMRSyncService.addVLToEncounter("Not detected", "400", "2009-08-01 00:00:00.0", encounter, order);
        Context.getObsService().getObservations("Anet Test Oloo");

        Assert.assertEquals(encounter, Context.getObsService().getObservations("Anet Test Oloo").get(1).getEncounter());
        List<Obs> obs=Context.getObsService().getObservationsByPersonAndConcept(encounter.getPatient().getPerson(),Context.getConceptService().getConcept(1305));
        Assert.assertTrue(obs.size()>0);
        Assert.assertEquals("1306",obs.get(0).getValueCoded().getConceptId().toString());

    }


    @Test
    public void getSyncFhirProfileById_ShouldReturnSyncFhirProfileByID() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncFhirProfile syncFhirProfile = ugandaEMRSyncService.getSyncFhirProfileById(1);

        Assert.assertNotNull(syncFhirProfile);
        Assert.assertEquals("Example Profile", syncFhirProfile.getName());
    }

    @Test
    public void getSyncFhirProfileByUUID_ShouldReturnSyncFhirProfileByUUID() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncFhirProfile syncFhirProfile = ugandaEMRSyncService.getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa");

        Assert.assertNotNull(syncFhirProfile);
        Assert.assertEquals("Example Profile", syncFhirProfile.getName());
    }


    @Test
    public void saveSyncFhirProfile_shouldSaveSyncFhirProfile() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncFhirProfile syncFhirProfile = new SyncFhirProfile();
        syncFhirProfile.setName("FHIR Profile to be saved");
        syncFhirProfile.setGenerateBundle(true);
        syncFhirProfile.setResourceTypes("Patient,Encounter,Observation");
        syncFhirProfile.setResourceSearchParameter(FHIR_FILTER_OBJECT_STRING);
        syncFhirProfile.setUrl("http://google.com");
        syncFhirProfile.setUrlUserName("username");
        syncFhirProfile.setUrlPassword("password");
        syncFhirProfile.setUrlToken("ZZZZAAAACCCC");

        syncFhirProfile = ugandaEMRSyncService.saveSyncFhirProfile(syncFhirProfile);

        Assert.assertNotNull(syncFhirProfile);
        Assert.assertNotNull(syncFhirProfile.getId());
        Assert.assertEquals(syncFhirProfile.getResourceSearchParameter(), FHIR_FILTER_OBJECT_STRING);
    }

    @Test
    public void getSyncFhirProfileByScheduledTaskName_ShouldReturnSyncFHIRFromScheduledTask() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        String encounterFilter = "{\"observationFilter\":{\"encounterReference\":[],\"patientReference\":[],\"hasMemberReference\":[],\"valueConcept\":[],\"valueDateParam\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"},\"valueQuantityParam\":[],\"valueStringParam\":[],\"date\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"},\"code\":[],\"category\":[],\"id\":[],\"lastUpdated\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"}},\"patientFilter\":{\"name\":[],\"given\":[],\"family\":[],\"identifier\":[],\"gender\":[],\"birthDate\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"},\"deathDate\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"},\"deceased\":[],\"city\":[],\"state\":[],\"postalCode\":[],\"country\":[],\"id\":[],\"lastUpdated\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"}},\"encounterFilter\":{\"date\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"},\"location\":[],\"participant\":[],\"subject\":[],\"id\":[],\"type\":[\"8d5b2be0-c2cc-11de-8d13-0010c6dffd0f\"],\"lastUpdated\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"}},\"personFilter\":{\"name\":[],\"gender\":[],\"birthDate\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"},\"deceased\":[],\"city\":[],\"state\":[],\"postalCode\":[],\"country\":[],\"id\":[],\"lastUpdated\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"}},\"practitionerFilter\":{\"identifier\":[],\"name\":[],\"given\":[],\"family\":[],\"deceased\":[],\"city\":[],\"state\":[],\"postalCode\":[],\"country\":[],\"id\":[],\"lastUpdated\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"}}}";
        SyncFhirProfile syncFhirProfile = ugandaEMRSyncService.getSyncFhirProfileByScheduledTaskName("Example Task for FHIR Exchange Profile");
        Assert.assertEquals(syncFhirProfile.getResourceSearchParameter(), encounterFilter);
        Assert.assertEquals("Example Profile", syncFhirProfile.getName());
    }


    @Test
    public void saveSyncFHIRResource_shouldSaveSyncFHIRResource() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        String sampleResource = "{\"resourceType\":\"Bundle\",\"type\":\"transaction\",\"entry\":[{\"resource\":{\"resourceType\":\"Observation\",\"id\":\"071ef75b-713d-4f52-adee-138646226512\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"160288AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Reason for appointment/visit\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"ab4510ac-3feb-4abb-8653-5d252813798f\",\"display\":\"ART Initiation\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"0ae0da82-915e-48a4-8d4c-ac80f6fac274\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"162476AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Specimen sources\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"162476\",\"display\":\"Specimen sources\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"159994AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Urine\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"159994\",\"display\":\"Urine\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"12c89990-1618-402d-bdc3-58e313d1860e\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"b04eaf95-77c9-456a-99fb-f668f58a9386\",\"display\":\"OTHER MEDICATIONS DISPENSED\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueString\":\"DTG\"},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"154dc2f2-39f7-45b4-aa55-76c58efdfba0\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"ddcd8aad-9085-4a88-a411-f19521be4785\",\"display\":\"HIV TEST\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"6d7a7a98-2c57-4318-9961-8e61fb427781\",\"display\":\"ANTIBODY HIV TEST\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"173d7b24-364a-457c-8b0b-8a214778cd0c\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"laboratory\",\"display\":\"Laboratory\"}]}],\"code\":{\"coding\":[{\"code\":\"dca16e53-30ab-102d-86b0-7a5022ba4115\",\"display\":\"HEPATITIS B TEST - QUALITATIVE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dc85aa72-30ab-102d-86b0-7a5022ba4115\",\"display\":\"NEGATIVE\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"1e06a662-608d-46eb-82c2-9485a8d064a7\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"165050AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Nutrition Assesment\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dc9816bd-30ab-102d-86b0-7a5022ba4115\",\"display\":\"NORMAL\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"1ef87b0e-cf8f-44f9-a675-0e5409987099\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"159368AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Medication duration\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":30.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"218f81d7-464f-4552-99e7-f6e9afde3805\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"7593ede6-6574-4326-a8a6-3d742e843659\",\"display\":\"ARV REGIMEN DAYS DISPENSED\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":30.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"252463a7-1914-4398-b0bd-cbe516f88e63\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"dcda9857-30ab-102d-86b0-7a5022ba4115\",\"display\":\"SCHEDULED PATIENT VISIST\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueBoolean\":true},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"3a8d75ac-3c31-412c-b49c-91417a98d46e\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Pulse\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5087\",\"display\":\"Pulse\"},{\"system\":\"http://loinc.org\",\"code\":\"8867-4\",\"display\":\"Pulse\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":5.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"3ca1be24-51b2-4bcf-9d93-75e0570274e5\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"ab505422-26d9-41f1-a079-c3d222000440\",\"display\":\"BASELINE REGIMEN START DATE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueDateTime\":\"2021-04-01T00:00:00+03:00\"},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"3f653fad-f8d9-4385-8292-0fc5a21a6239\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"ffe9b82c-d341-47a9-a7ef-89c0f5abba97\",\"display\":\"ARV MED SET\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"hasMember\":[{\"reference\":\"Observation/46374bcb-b5f3-4caf-b336-a819558ffb5d\",\"type\":\"Observation\"},{\"reference\":\"Observation/fb08ae34-51c1-4379-b05a-93cff4a1c4fb\",\"type\":\"Observation\"},{\"reference\":\"Observation/218f81d7-464f-4552-99e7-f6e9afde3805\",\"type\":\"Observation\"}]},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"3fa65d9f-7f43-4d16-a1bd-dcd7a243aca8\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Height (cm)\"},{\"system\":\"http://loinc.org\",\"code\":\"8302-2\",\"display\":\"Height (cm)\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5090\",\"display\":\"Height (cm)\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":167.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"403477b3-53de-4584-a6c8-f60c947ff251\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dc7620b3-30ab-102d-86b0-7a5022ba4115\",\"display\":\"METHOD OF FAMILY PLANNING\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dc692ad3-30ab-102d-86b0-7a5022ba4115\",\"display\":\"CONDOMS\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"4103a799-40cb-4428-883e-50263ed5e03e\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"31c5c7aa-4948-473e-890b-67fe2fbbd71a\",\"display\":\"HIV ENROLLMENT DATE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueDateTime\":\"2021-04-01T00:00:00+03:00\"},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"46374bcb-b5f3-4caf-b336-a819558ffb5d\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"dd2b0b4d-30ab-102d-86b0-7a5022ba4115\",\"display\":\"CURRENT ARV REGIMEN\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"a779d984-9ccf-4424-a750-47506bf8212b\",\"display\":\"AZT/3TC/DTG\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"48398e3c-96a7-4e9c-bc8b-39c328d8dded\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Diastolic blood pressure\"},{\"system\":\"http://loinc.org\",\"code\":\"35094-2\",\"display\":\"Diastolic blood pressure\"},{\"system\":\"http://loinc.org\",\"code\":\"8462-4\",\"display\":\"Diastolic blood pressure\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5086\",\"display\":\"Diastolic blood pressure\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":120.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"4aa46edb-d254-4cc6-bda9-b21576c56dbc\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dce0a659-30ab-102d-86b0-7a5022ba4115\",\"display\":\"FAMILY PLANNING STATUS\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcdd6c4a-30ab-102d-86b0-7a5022ba4115\",\"display\":\"NOT PREGNANT AND ON FAMILY PLANNING\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"4b3d3816-2399-4e34-b0b0-fbdb113c758f\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Weight (kg)\"},{\"system\":\"http://loinc.org\",\"code\":\"3141-9\",\"display\":\"Weight (kg)\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5089\",\"display\":\"Weight (kg)\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":66.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"51de7299-0b09-49ca-950f-08860cb26e01\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dfc50562-da6a-4ce2-ab80-43c8f2d64d6f\",\"display\":\"Quantity Unit\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"1513AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Tablet\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"1513\",\"display\":\"Tablet\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"5a98e805-dc69-4361-8563-41089a8d3992\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dcdff274-30ab-102d-86b0-7a5022ba4115\",\"display\":\"WHO HIV CLINICAL STAGE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcda2bc2-30ab-102d-86b0-7a5022ba4115\",\"display\":\"HIV WHO CLINICAL STAGE 1\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"68d8bf28-8eb3-4ca5-81e8-a368a8d7769b\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"1343AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"MID-UPPER ARM CIRCUMFERENCE\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"1343\",\"display\":\"MID-UPPER ARM CIRCUMFERENCE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":22.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"6924f8e9-b17f-460b-bd01-23bd81a53d58\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Temperature (C)\"},{\"system\":\"http://loinc.org\",\"code\":\"8310-5\",\"display\":\"Temperature (C)\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5088\",\"display\":\"Temperature (C)\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":36.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"74655372-0f65-4b23-b468-510fa1c3a5b0\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"d8bc9915-ed4b-4df9-9458-72ca1bc2cd06\",\"display\":\"Syphilis test result for partner\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"5bc3446f-c473-4f6c-ba58-a168ea79f096\",\"display\":\"No clinical Symptoms and Signs\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"787a96e9-9f9c-425e-b8ab-5e13a1d53a15\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dcac04cf-30ab-102d-86b0-7a5022ba4115\",\"display\":\"RETURN VISIT DATE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueDateTime\":\"2021-04-30T00:00:00+03:00\"},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"850eb0ac-adc7-4ea7-b04a-260c42755ad1\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"dce12b4f-30ab-102d-86b0-7a5022ba4115\",\"display\":\"DATE POSITIVE HIV TEST CONFIRMED\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueDateTime\":\"2021-04-01T00:00:00+03:00\"},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"861b0643-b417-4ef2-b8d9-c005c52048ab\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Systolic blood pressure\"},{\"system\":\"http://loinc.org\",\"code\":\"8480-6\",\"display\":\"Systolic blood pressure\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5085\",\"display\":\"Systolic blood pressure\"},{\"system\":\"http://loinc.org\",\"code\":\"53665-6\",\"display\":\"Systolic blood pressure\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":90.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"8661d536-9446-465c-bd74-64f06fc205a2\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"laboratory\",\"display\":\"Laboratory\"}]}],\"code\":{\"coding\":[{\"code\":\"dca16e53-30ab-102d-86b0-7a5022ba4115\",\"display\":\"HEPATITIS B TEST - QUALITATIVE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"159971AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"waiting for test results\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"8af49d68-dd89-4be1-853d-75d17a7da3b3\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dce05b7f-30ab-102d-86b0-7a5022ba4115\",\"display\":\"MEDICATION OR OTHER SIDE EFFECTS\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcdd99e5-30ab-102d-86b0-7a5022ba4115\",\"display\":\"NAUSEA\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"8f3f3e37-5e67-453a-831d-16f53290390d\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"1732AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Duration units\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"1732\",\"display\":\"Duration units\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"1072AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Days\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"1072\",\"display\":\"Days\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"90d645ec-94f8-4e1f-ad1e-36ef0ffd262e\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"8531d1a7-9793-4c62-adab-f6716cf9fabb\",\"display\":\"NUTRITION SUPPORT AND INFANT FEEDING\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"598dba00-b878-474c-9a10-9998f1748228\",\"display\":\"THERAPEUTIC FEEDING\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"979ff44c-bf69-4b63-b25c-901d39552869\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"17def5f6-d6b4-444b-99ed-40eb05d2c4f8\",\"display\":\"Advanced Disease Status\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"7e5beae4-7244-4c73-b4b2-cbaf11771f21\",\"display\":\"No Advanced Disease\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"9a95ccd1-53b1-44f0-924e-1a42af59c3e8\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Respiratory rate\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5242\",\"display\":\"Respiratory rate\"},{\"system\":\"http://loinc.org\",\"code\":\"9279-1\",\"display\":\"Respiratory rate\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":2.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"a47a4d90-ca96-479a-a123-8810f5786333\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"7c9bde8d-a5a7-473f-99d5-4991dc6feb01\",\"display\":\"Other Drug Dispensed Set\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueString\":\"Days, 30.0, Tablet, DTG, 30.0\",\"hasMember\":[{\"reference\":\"Observation/8f3f3e37-5e67-453a-831d-16f53290390d\",\"type\":\"Observation\"},{\"reference\":\"Observation/d5fb05ec-ba24-426d-adb3-ff5987e8c1e8\",\"type\":\"Observation\"},{\"reference\":\"Observation/51de7299-0b09-49ca-950f-08860cb26e01\",\"type\":\"Observation\"},{\"reference\":\"Observation/12c89990-1618-402d-bdc3-58e313d1860e\",\"type\":\"Observation\"},{\"reference\":\"Observation/1ef87b0e-cf8f-44f9-a675-0e5409987099\",\"type\":\"Observation\"}]},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"b4f464a3-9beb-4f2d-a051-8be528b87d74\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dcdfe3ce-30ab-102d-86b0-7a5022ba4115\",\"display\":\"ENTRY POINT INTO HIV CARE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcd7e8e5-30ab-102d-86b0-7a5022ba4115\",\"display\":\"PMTCT\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"b58d4292-722e-4a01-99e3-3ccc419ddfd0\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dce02aa1-30ab-102d-86b0-7a5022ba4115\",\"display\":\"TUBERCULOSIS STATUS\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcdaccc1-30ab-102d-86b0-7a5022ba4115\",\"display\":\"No signs or symptoms of TB\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"ba80aa99-b3e5-40b4-bc58-36ca617ae172\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5f86d19d-9546-4466-89c0-6f80c101191b\",\"display\":\"MID-UPPER ARM CIRCUMFERENCE-CODE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"8846c03f-67bf-4aeb-8ca7-39bf79b4ebf3\",\"display\":\"GREEN\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"c2c628f7-516d-41c6-a870-bfd1e9bec618\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dce03b2f-30ab-102d-86b0-7a5022ba4115\",\"display\":\"ANTI-RETROVIRAL DRUG ADHERENCE ASSESSMENT CODE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcdf1708-30ab-102d-86b0-7a5022ba4115\",\"display\":\"GOOD ADHERENCE\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"cc8806af-a6c0-47c8-b7b6-3fe79c7a948f\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"39243cef-b375-44b1-9e79-cbf21bd10878\",\"display\":\"BASELINE STAGE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dc9b8cd1-30ab-102d-86b0-7a5022ba4115\",\"display\":\"WHO STAGE 1 ADULT\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"d5fb05ec-ba24-426d-adb3-ff5987e8c1e8\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"160856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Quantity of medication prescribed per dose\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":30.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"da1199d4-1fa0-4b88-bbd8-9d5751065177\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"c3332e8d-2548-4ad6-931d-6855692694a3\",\"display\":\"BASELINE REGIMEN\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"a779d984-9ccf-4424-a750-47506bf8212b\",\"display\":\"AZT/3TC/DTG\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"e39f9a83-1089-4020-89d4-afc8a3f6dd27\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"cabfa0e9-ddae-438b-a052-6d5c97164242\",\"display\":\"CARE ENTRY POINT SET\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueString\":\"PMTCT\",\"hasMember\":[{\"reference\":\"Observation/b4f464a3-9beb-4f2d-a051-8be528b87d74\",\"type\":\"Observation\"}]},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"e4344951-30f5-424a-8ea1-78b8e8885beb\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dca07f4a-30ab-102d-86b0-7a5022ba4115\",\"display\":\"TESTS ORDERED\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"1eb05918-f50c-4cad-a827-3c78f296a10a\",\"display\":\"Viral Load Test\"},{\"system\":\"http://loinc.org\",\"code\":\"315124004\",\"display\":\"Viral Load Test\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"e76e86ba-adc3-4e37-9d61-d00aaf1d0269\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"e525c286-74b2-4e30-84ac-c4d5f07c503c\",\"display\":\"BASELINE REGIMEN SET\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueString\":\"AZT/3TC/DTG, 2021-04-01\",\"hasMember\":[{\"reference\":\"Observation/da1199d4-1fa0-4b88-bbd8-9d5751065177\",\"type\":\"Observation\"},{\"reference\":\"Observation/3ca1be24-51b2-4bcf-9d93-75e0570274e5\",\"type\":\"Observation\"}]},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"fb08ae34-51c1-4379-b05a-93cff4a1c4fb\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"b0e53f0a-eaca-49e6-b663-d0df61601b70\",\"display\":\"AR REGIMEN DOSE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":30.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"fb9d5a15-c7cd-44f3-8206-df6d15e2b5d4\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dce0e02a-30ab-102d-86b0-7a5022ba4115\",\"display\":\"SYMPTOM, DIAGNOSIS, OR OPPORTUNISTIC INFECTION\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcdebc02-30ab-102d-86b0-7a5022ba4115\",\"display\":\"ULCERS\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"fc1348bf-4eec-4276-83d9-24517ba88c6a\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"37d4ac43-b3b4-4445-b63b-e3acf47c8910\",\"display\":\"TPT STATUS\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"1090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Never\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"fdb56b9f-58ae-45bc-8521-ad6cc09ba4cd\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"0f998893-ab24-4ee4-922a-f197ac5fd6e6\",\"display\":\"Lab Number\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueString\":\"224484864\"},\"request\":{\"method\":\"POST\"}}]}";

        SyncFhirResource syncFHIRResource = new SyncFhirResource();

        syncFHIRResource.setGeneratorProfile(Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa"));
        syncFHIRResource.setSynced(false);
        syncFHIRResource.setResource(sampleResource);
        syncFHIRResource = ugandaEMRSyncService.saveFHIRResource(syncFHIRResource);

        Assert.assertNotNull(syncFHIRResource);
        Assert.assertNotNull(syncFHIRResource.getId());
        Assert.assertEquals(syncFHIRResource.getGeneratorProfile().getName(), "Example Profile");
    }

    @Test
    public void getSyncFHIRResourceBySyncFhirProfileUUID_shouldGetResourcesGeneratedBySyncFhirProfile() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncFhirProfile syncFhirProfile = Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa");

        List<SyncFhirResource> syncFhirResources = ugandaEMRSyncService.getSyncFHIRResourceBySyncFhirProfile(syncFhirProfile, false);

        Assert.assertNotNull(syncFhirResources);
        Assert.assertEquals("Example Profile", syncFhirResources.get(0).getGeneratorProfile().getName());
    }

    @Test
    public void getSyncFHIRResourceById_shouldGetResources() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncFhirResource syncFhirResources = ugandaEMRSyncService.getSyncFHIRResourceById(1);

        Assert.assertNotNull(syncFhirResources);
        Assert.assertEquals("Example Profile", syncFhirResources.getGeneratorProfile().getName());
    }

    @Test
    public void markSyncFHIRResourceSynced_shouldMarkSyncFHIRResourceSynced() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncFhirResource syncFhirResources = ugandaEMRSyncService.getSyncFHIRResourceById(1);
        Assert.assertFalse(syncFhirResources.getSynced());

        SyncFhirResource markedSyncFhirResource = ugandaEMRSyncService.markSyncFHIRResourceSynced(syncFhirResources);

        Assert.assertTrue(markedSyncFhirResource.getSynced());
        Assert.assertNotNull(markedSyncFhirResource.getDateSynced());
        Assert.assertNotNull(markedSyncFhirResource.getExpiryDate());
        Integer daysToDeletion = Math.toIntExact(((markedSyncFhirResource.getExpiryDate().getTime() - markedSyncFhirResource.getDateSynced().getTime()) / (1000 * 60 * 60 * 24)));
        Assert.assertEquals(daysToDeletion, syncFhirResources.getGeneratorProfile().getDurationToKeepSyncedResources());

    }

    @Test
    public void saveSyncFhirProfileLog_shouldSaveSyncFhirProfileLog() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);


        SyncFhirProfileLog syncFhirProfileLog = new SyncFhirProfileLog();

        syncFhirProfileLog.setLastGenerationDate(new Date());
        syncFhirProfileLog.setResourceType("Encounter");
        syncFhirProfileLog.setProfile(Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa"));
        syncFhirProfileLog.setNumberOfResources(5);
        SyncFhirProfileLog syncFhirProfileLog1 = ugandaEMRSyncService.saveSyncFhirProfileLog(syncFhirProfileLog);
        Assert.assertNotNull(syncFhirProfileLog1);
        Assert.assertNotNull(syncFhirProfileLog1.getId());
        Assert.assertEquals(syncFhirProfileLog1.getResourceType(), "Encounter");
    }


    @Test
    public void getSyncFhirProfileLogByProfileAndResourceName_shouldGetSyncFhirProfileLog() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncFhirProfileLog syncFhirProfileLog = ugandaEMRSyncService.getLatestSyncFhirProfileLogByProfileAndResourceName(Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa"), "Encounter");

        Assert.assertNotNull(syncFhirProfileLog);

    }

    @Test
    public void getSyncFHIRCaseBySyncFhirProfileAndPatient_ShouldGetSyncFHIRCase() {
        String patientUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
        String caseIdentifier = "ART-MALE-1";
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        Patient patient = Context.getPatientService().getPatientByUuid(patientUID);
        SyncFhirProfile syncFhirProfile = Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa");
        SyncFhirCase syncFHIRCase = ugandaEMRSyncService.getSyncFHIRCaseBySyncFhirProfileAndPatient(syncFhirProfile, patient, caseIdentifier);

        Assert.assertNotNull(syncFHIRCase);
        Assert.assertEquals(syncFHIRCase.getPatient().getUuid(), patientUID);

    }

    @Test
    public void saveSyncFHIRCase_shouldSyncFHIRCase() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncFhirCase syncFHIRCase = new SyncFhirCase();
        Date date = new Date();

        syncFHIRCase.setPatient(Context.getPatientService().getPatient(2));
        syncFHIRCase.setProfile(ugandaEMRSyncService.getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa"));
        syncFHIRCase.setCaseIdentifier(Context.getPatientService().getPatient(2).getPatientIdentifier(4).getIdentifier());
        syncFHIRCase.setLastUpdateDate(date);

        SyncFhirCase syncFhirCase1 = ugandaEMRSyncService.saveSyncFHIRCase(syncFHIRCase);

        Assert.assertNotNull(syncFhirCase1);
        Assert.assertNotNull(syncFhirCase1.getCaseId());
        Assert.assertNotNull(syncFhirCase1.getCaseIdentifier());
        Assert.assertNotNull("ART-MALE-1", syncFhirCase1.getCaseIdentifier());
    }

    @Test
    public void addTestResultsToEncounter_shouldSaveCD4ResultsToEncounter() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        Order order = Context.getOrderService().getOrder(6);

        int initialObsCount = order.getEncounter().getObs().size();

        // Now passing raw JSON string instead of JSONObject
        ugandaEMRSyncService.addTestResultsToEncounter(sampleResultsForCd4Test, order);

        int updatedObsCount = order.getEncounter().getObs().size();
        Set<Integer> conceptIds = order.getEncounter().getObs().stream()
                .map(obs -> obs.getConcept().getConceptId())
                .collect(Collectors.toSet());

        Set<Double> numericValues = order.getEncounter().getObs().stream()
                .map(Obs::getValueNumeric)
                .collect(Collectors.toSet());

        Assert.assertTrue(updatedObsCount > initialObsCount);
        Assert.assertTrue(conceptIds.contains(cd4Concept));
        Assert.assertTrue(numericValues.contains(400.0));
    }


    @Test
    public void addTestResultsToEncounter_shouldSaveCBCResultsWithKnownOrderToEncounter() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        Order order = Context.getOrderService().getOrder(15);

        Integer numberOfObs = order.getEncounter().getObs().size();

        ugandaEMRSyncService.addTestResultsToEncounter(sampleResultsForCBCTest, order);

        Assert.assertTrue(Context.getOrderService().getOrder(15).getEncounter().getObs().size() > numberOfObs);
        Assert.assertTrue(order.getEncounter().getObs().stream().map(Obs::getConcept).collect(Collectors.toSet()).contains(Context.getConceptService().getConcept(WBCCountConcept)));
        Assert.assertTrue(order.getEncounter().getObs().stream().map(Obs::getValueNumeric).collect(Collectors.toSet()).contains(7.26));
    }

    @Test
    public void addTestResultsToEncounter_shouldSaveCBCResultsWithOutKnownOrderToEncounter() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        List<Encounter> encounters = ugandaEMRSyncService.addTestResultsToEncounter(sampleResultsForCBCTest, null);
        Order cbcOrder = new ArrayList<>(encounters.get(0).getOrders()).get(0);
        Assert.assertNotNull(cbcOrder);
        Assert.assertTrue(cbcOrder.getEncounter().getObs().stream().map(Obs::getConcept).collect(Collectors.toSet()).contains(Context.getConceptService().getConcept(WBCCountConcept)));
        Assert.assertTrue(cbcOrder.getEncounter().getObs().stream().map(Obs::getValueNumeric).collect(Collectors.toSet()).contains(7.26));
    }

    @Test
    public void testisValidCPHLBarCodeValidCurrentYearBarcode() {
        String barcode = String.format("%02d1234789", currentYearSuffix);
        Assert.assertTrue(ugandaEMRSyncService.isValidCPHLBarCode(barcode));
    }

    @Test
    public void testisValidCPHLBarCodeValidPreviousYearBarcode() {
        String barcode = String.format("%02d5678093", currentYearSuffix - 1);
        Assert.assertTrue(ugandaEMRSyncService.isValidCPHLBarCode(barcode));
    }

    @Test
    public void testisValidCPHLBarCodeInvalidOlderBarcode() {
        Assert.assertFalse(ugandaEMRSyncService.isValidCPHLBarCode("2312348471")); // Assuming year is 2025
    }

    @Test
    public void testisValidCPHLBarCodeBarcodeWithNonNumericPrefix() {
        Assert.assertFalse(ugandaEMRSyncService.isValidCPHLBarCode("AB12340921"));
    }

    @Test
    public void testisValidCPHLBarCodeTooShortBarcode() {
        Assert.assertFalse(ugandaEMRSyncService.isValidCPHLBarCode("1"));
    }

    @Test
    public void testisValidCPHLBarCodeNullBarcode() {
        Assert.assertFalse(ugandaEMRSyncService.isValidCPHLBarCode(null));
    }

    @Test
    public void testisValidCPHLBarCodeEmptyBarcode() {
        Assert.assertFalse(ugandaEMRSyncService.isValidCPHLBarCode(""));
    }


    @Test
    public void testGetReferralOrderConcepts_GlobalPropertyHasOneOrMore() {

        List<Concept> concepts= ugandaEMRSyncService.getReferralOrderConcepts();

        Assert.assertEquals(1,concepts.size());
        Assert.assertNotNull("Concept returned are not null",concepts.get(0));
    }

    @Test
    public void testGetReferralOrderConcepts_GlobalPropertyValueIsBlank() {
        GlobalProperty globalProperty=Context.getAdministrationService().getGlobalPropertyObject("ugandaemrsync.cphlReferralOrderConceptIds");

        Assert.assertEquals(globalProperty.getPropertyValue(),"165412");

        globalProperty.setPropertyValue("");

        Context.getAdministrationService().saveGlobalProperty(globalProperty);

        List<Concept> concepts= ugandaEMRSyncService.getReferralOrderConcepts();

        Assert.assertEquals(0,concepts.size());
    }


}
