package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.api.impl.UgandaEMRSyncServiceImpl;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.server.SyncConstant;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.util.UgandaEMRSyncUtil;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.*;

/**
 * Posts Viral load data to the central server
 */

public class SendViralLoadRequestToCentralServerTask extends AbstractTask {

    protected Log log = LogFactory.getLog(SendViralLoadRequestToCentralServerTask.class);

    @Override
    public void execute() {
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        List<Order> orderList = new ArrayList<>();

        if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
            return;
        }

        try {
            orderList = getOrders();
        } catch (IOException e) {
            log.error("Failed to get orders", e);
        } catch (ParseException e) {
            log.error("Failed to pass orders to list", e);
        }

        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID);

        for (Order order : orderList) {
            List<SyncTask> allSyncTasks = ugandaEMRSyncService.getAllSyncTask();
            List<SyncTask> syncTasks = allSyncTasks.stream().filter(p -> order.getAccessionNumber().equals(p.getSyncTask()) && syncTaskType.getId().equals(p.getSyncTaskType().getId())).collect(Collectors.toList());

            if (syncTasks.size()<1){
                Map<String, String> dataOutput = generateVLFHIROrderTestRequestBody((TestOrder) order, VL_SEND_SAMPLE_FHIR_JSON_STRING);
                String json = dataOutput.get("json");

                try {
                    Map map = ugandaEMRHttpURLConnection.sendPostBy(syncTaskType.getUrl(), syncTaskType.getUrlUserName(), syncTaskType.getUrlPassword(), "", json, false);
                    if (map != null) {
                        SyncTask newSyncTask = new SyncTask();
                        newSyncTask.setDateSent(new Date());
                        newSyncTask.setCreator(Context.getUserService().getUser(1));
                        newSyncTask.setSentToUrl(syncTaskType.getUrl());
                        newSyncTask.setRequireAction(true);
                        newSyncTask.setActionCompleted(false);
                        newSyncTask.setSyncTask(order.getAccessionNumber());
                        newSyncTask.setStatusCode((Integer) map.get("responseCode"));
                        newSyncTask.setStatus((String)map.get("responseMessage"));
                        newSyncTask.setSyncTaskType(ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID));
                        ugandaEMRSyncService.saveSyncTask(newSyncTask);
                    }
                } catch (Exception e) {
                    log.error("Failed to create sync task",e);
                }
            }
        }
    }

    /**
     * @return
     */
    private List<List<Object>> getViralLoadRequestData() {
        return Context.getAdministrationService().executeSQL(SyncConstant.VIRAL_LOAD_ENCOUNTER_QUERY, false);
    }

    /**
     * Generate VL test Request
     *
     * @param encounter
     * @return
     */
    public Map<String, String> generateVLFHIRTestRequestBody(Encounter encounter, String jsonFhirMap) {
        Map<String, String> jsonMap = new HashMap<>();
        UgandaEMRSyncService ugandaEMRSyncService = new UgandaEMRSyncServiceImpl();
        String filledJsonFile = "";
        if (encounter != null && encounter.getEncounterId() != null) {
            String obsSampleType = "";
            String obsRequesterContact = "";
            String healthCenterName = ugandaEMRSyncService.getHealthCenterName();
            String healthCenterCode = ugandaEMRSyncService.getHealthCenterCode();
            String requestType = encounter.getEncounterType().getName();
            String sourceSystem = "UgandaEMR";
            String patientARTNO = ugandaEMRSyncService.getPatientIdentifier(encounter.getPatient(),PATIENT_IDENTIFIER_TYPE);
            String sampleID = encounter.getEncounterId().toString();
            String sampleCollectionDate = encounter.getEncounterDatetime().toString();
            String clinicianNames = getProviderByEncounterRole(encounter, "clinician");
            String labTechNames = getProviderByEncounterRole(encounter, "Lab Technician");


            for (Obs obs : encounter.getAllObs()) {
                if (obs.getConcept().getConceptId() == 165153) {
                    obsSampleType = obs.getValueCoded().getName().getName();
                }
                if (obs.getConcept().getConceptId() == 159635) {
                    obsRequesterContact = obs.getValueText();
                }
            }

            filledJsonFile = String.format(jsonFhirMap, healthCenterCode, healthCenterName, requestType, sourceSystem, patientARTNO, sampleID, obsSampleType, sampleCollectionDate, clinicianNames, obsRequesterContact, labTechNames, "None", "CPHL");
        }
        jsonMap.put("json", filledJsonFile);
        return jsonMap;
    }

    /**
     * Gererates FHIR MESSAGE Basing On Order To Lab That is refereed to Reference Lab
     *
     * @param testOrder
     * @param jsonFHIRMap
     * @return
     */
    public Map<String, String> generateVLFHIROrderTestRequestBody(TestOrder testOrder, String jsonFHIRMap) {
        Map<String, String> jsonMap = new HashMap<>();
        UgandaEMRSyncService ugandaEMRSyncService = new UgandaEMRSyncServiceImpl();
        String filledJsonFile = "";
        if (testOrder != null) {



            String healthCenterName = ugandaEMRSyncService.getHealthCenterName();
            String healthCenterCode = ugandaEMRSyncService.getHealthCenterCode();
            String requestType = proccessMappings(testOrder.getConcept());
            String sourceSystem = "UgandaEMR";
            String patientARTNO = ugandaEMRSyncService.getPatientIdentifier(testOrder.getPatient(),PATIENT_IDENTIFIER_TYPE);
            String sampleID = testOrder.getAccessionNumber();
            String sampleCollectionDate = testOrder.getEncounter().getEncounterDatetime().toString();
            String clinicianNames = testOrder.getOrderer().getName();
            String labTechNames = testOrder.getCreator().getPersonName().getFullName();
            String labTechContact = "None";
            String ordererContact = "None";

            try {
                if (getProviderAttributeValue(Objects.requireNonNull(getProviderAppributesFromPerson(testOrder.getCreator().getPerson()))) != null) {
                    labTechContact = getProviderAttributeValue(Objects.requireNonNull(getProviderAppributesFromPerson(testOrder.getCreator().getPerson())));
                }
            }catch (Exception e){
                log.error("Could not add Lab technician telephone number",e);
            }

            String obsSampleType = testOrder.getSpecimenSource().getName().getName();
            if (getProviderAttributeValue(testOrder.getOrderer().getActiveAttributes()) != null) {
                ordererContact = getProviderAttributeValue(testOrder.getOrderer().getActiveAttributes());
            }

            filledJsonFile = String.format(jsonFHIRMap, healthCenterCode, healthCenterName, requestType, sourceSystem, patientARTNO, sampleID, obsSampleType, sampleCollectionDate, labTechNames, labTechContact,sampleCollectionDate, clinicianNames, ordererContact, "CPHL");
        }
        jsonMap.put("json", filledJsonFile);
        return jsonMap;
    }

    private String proccessMappings(Concept concept) {
        for (ConceptMap conceptMap : concept.getConceptMappings()) {
            return conceptMap.getConceptReferenceTerm().getCode();
        }
        return null;
    }

    private String getProviderByEncounterRole(Encounter encounter, String encounterRoleName) {
        for (EncounterProvider provider : encounter.getActiveEncounterProviders()) {
            if (provider.getEncounterRole().getName() == encounterRoleName) {
                return provider.getProvider().getName();
            }
        }
        return null;
    }

    public List<Order> getOrders() throws IOException, ParseException {
        OrderService orderService = Context.getOrderService();
        List<Order> orders = new ArrayList<>();
        List list = Context.getAdministrationService().executeSQL(VIRAL_LOAD_ORDERS_QUERY, true);
        if (list.size() > 0) {
            for (Object o : list) {
                Order order = orderService.getOrder(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString()));
                if (order.getAccessionNumber() != null && order.isActive() && order.getInstructions().equalsIgnoreCase("REFER TO cphl")) {
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    private String getProviderAttributeValue(Collection<ProviderAttribute> providerAttributes) {
        for (ProviderAttribute providerAttribute : providerAttributes) {
            if (providerAttribute.getAttributeType().getName().equals("Phone Number")) {
                return providerAttribute.getValue().toString();
            }

        }
        return null;
    }

    private Collection<ProviderAttribute> getProviderAppributesFromPerson(Person person) {
        List<Provider> providers = (List<Provider>) Context.getProviderService().getProvidersByPerson(person);
        if (providers != null) {
            return providers.get(0).getActiveAttributes();
        }
        return null;
    }

}
