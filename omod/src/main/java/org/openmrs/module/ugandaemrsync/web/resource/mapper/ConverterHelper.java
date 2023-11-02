package org.openmrs.module.ugandaemrsync.web.resource.mapper;

import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.model.SyncFhirResource;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.web.resource.DTO.FhirResourceDetails;
import org.openmrs.module.ugandaemrsync.web.resource.DTO.SyncTaskDetails;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.*;

public class ConverterHelper {
    public static SyncTaskDetails convertSyncTaskDetails(SyncTask syncTask) {

        if (Objects.equals(syncTask.getSyncTaskType().getUuid(), VIRAL_LOAD_SYNC_TYPE_UUID)) {
            PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(PATIENT_IDENTIFIER_TYPE);
            String accessionNumber = syncTask.getSyncTask();
            Patient patient = getPatientByAccessionNumber(accessionNumber);
            Date dateSent = syncTask.getDateSent();
            int statusCode = syncTask.getStatusCode();

            String status = convertStatusCode(statusCode);
            PatientIdentifier pi = patient.getPatientIdentifier(identifierType);
            String identifier = "";
            if (pi != null) {
                identifier = pi.getIdentifier();
            }
            return new SyncTaskDetails(patient.getPersonName().getFullName(), identifier, statusCode, status, dateSent,patient.getUuid());
        } if(Objects.equals(syncTask.getSyncTaskType().getUuid(), VIRAL_LOAD_RESULT_PULL_TYPE_UUID)){
            PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(PATIENT_IDENTIFIER_TYPE);
            String accessionNumber = syncTask.getSyncTask();
            Patient patient = getPatientByAccessionNumber(accessionNumber);
            Date dateSent = syncTask.getDateSent();
            int statusCode = syncTask.getStatusCode();
            String statusMessage = syncTask.getStatus();
            String status = convertStatusCode(statusCode);
            PatientIdentifier pi = patient.getPatientIdentifier(identifierType);
            String identifier = "";
            if (pi != null) {
                identifier = pi.getIdentifier();
            }
            return new SyncTaskDetails(patient.getPersonName().getFullName(), identifier, status, statusCode, dateSent,statusMessage,patient.getUuid());
        }
        else {
            return null;
        }
    }

    public static FhirResourceDetails convertSyncFhirResourceDetails(SyncFhirResource syncFhirResource) {

        PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(UIC_IDENTIFIER_TYPE);

        Patient patient = syncFhirResource.getPatient();
        Date dateSent = syncFhirResource.getDateSynced();
        Date dateCreated = syncFhirResource.getDateCreated();
        Integer statusCode = syncFhirResource.getStatusCode();
        String status= "";
        if(statusCode!=null){
            status = convertStatusCode(statusCode);
        }else{
            statusCode = 0;
        }
        String identifier = "";
        String name = "";
        String uuid ="";
        if (patient != null) {
            name = patient.getPersonName().getFullName();
            uuid = patient.getUuid();
            PatientIdentifier pi = patient.getPatientIdentifier(identifierType);

            if (pi != null) {
                identifier = pi.getIdentifier();
            }
        }

        return new FhirResourceDetails(name, identifier, statusCode, status, dateCreated, dateSent, uuid);

    }

    public static List<SyncTaskDetails> convertSyncTasks(List<SyncTask> syncTasks) {
        List<SyncTaskDetails> result = new ArrayList<>();
        if (syncTasks.size() > 0) {
            for (SyncTask syncTask : syncTasks) {
                SyncTaskDetails syncTaskDetail = convertSyncTaskDetails(syncTask);
                result.add(syncTaskDetail);
            }
        }
        return result;
    }


    public static List<FhirResourceDetails> convertSyncFhirResources(List<SyncFhirResource> syncFhirResources) {
        List<FhirResourceDetails> result = new ArrayList<>();
        if (syncFhirResources.size() > 0) {
            for (SyncFhirResource syncFhirResource : syncFhirResources) {
                FhirResourceDetails fhirResourceDetails = convertSyncFhirResourceDetails(syncFhirResource);
                result.add(fhirResourceDetails);
            }
        }
        return result;
    }

    public static String convertStatusCode(int statusCode) {
        if (statusCode == 200 || statusCode == 201) {
            return "Successfully sent";
        } else if (statusCode == 401) {
            return "User password or user name is incorrect ";
        } else if (statusCode == 500) {
            return "Error at server ";
        } else {
            return String.valueOf(statusCode);
        }
    }

    public static Patient getPatientByAccessionNumber(String accessionNumber) {
        PatientService patientService = Context.getPatientService();
        List<Order> orders = new ArrayList<>();
        String query = "SELECT patient_id from orders WHERE concept_id=165412 and accession_number= '" + accessionNumber + "' LIMIT 1";
        List list = Context.getAdministrationService().executeSQL(query, true);
        Patient patient = null;
        if (list.size() > 0) {
            patient = patientService.getPatient(Integer.parseUnsignedInt(((ArrayList) list.get(0)).get(0).toString()));
        }
        return patient;
    }

}
