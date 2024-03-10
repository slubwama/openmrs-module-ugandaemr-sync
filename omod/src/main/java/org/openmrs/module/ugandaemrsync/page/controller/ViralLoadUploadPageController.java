package org.openmrs.module.ugandaemrsync.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Order;
import org.openmrs.Patient;

import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collection;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.HIV_ENCOUNTER_PAGE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_FACILITY_DHIS2_ID_CELL_NO;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_FACILITY_NAME_CELL_NO;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_DATE_COLLECTION_CELL_NO;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_PATIENT_ART_ID_CELL_NO;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_RESULTS_NUMERIC_CELL_NO;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_RESULTS_ALHPA_NUMERIC_CELL_NO;

public class ViralLoadUploadPageController {
    protected final Log log = LogFactory.getLog(ViralLoadUploadPageController.class);

    private List<String> noEncounterFound = new ArrayList<>();

    private List<String> noPatientFound = new ArrayList<>();

    private List<String> patientResultNotReleased = new ArrayList<>();

    private String healthCenterNameValidator = "";

    public void get(@SpringBean PageModel pageModel, @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride) {
        pageModel.put("breadcrumbOverride", breadcrumbOverride);
        pageModel.put("noEncounterFound", this.noEncounterFound);
        pageModel.put("noPatientFound", this.noPatientFound);
        pageModel.put("patientResultNotReleased", this.patientResultNotReleased);
        pageModel.put("healthCenterNameValidator", this.healthCenterNameValidator);
        pageModel.put("errorMessage", "");
    }

    public void post(@SpringBean PageModel pageModel, UiUtils ui, @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride, @RequestParam(value = "returnUrl", required = false) String returnUrl, @RequestParam(value = "file", required = false) MultipartFile file) {

        readCSVFile(file, pageModel);
        pageModel.put("breadcrumbOverride", breadcrumbOverride);
        pageModel.put("noEncounterFound", this.noEncounterFound);
        pageModel.put("noPatientFound", this.noPatientFound);
        pageModel.put("patientResultNotReleased", this.patientResultNotReleased);
        pageModel.put("healthCenterNameValidator", this.healthCenterNameValidator);
    }

    private void readCSVFile(MultipartFile csvFile, PageModel pageModel) {

        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        List<Patient> noEncounterFound = new ArrayList<>();
        List<String> noPatientFound = new ArrayList<>();
        List<Patient> patientResultNotReleased = new ArrayList<>();
        String cvsSplitBy = ",";
        InputStream is = null;
        String errorMessage;
        try {
            is = csvFile.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            Object[] objects = br.lines().toArray();
            Collection<EncounterType> encounterTypes = ugandaEMRSyncService.getEcounterTypes(HIV_ENCOUNTER_PAGE_UUID);


            if (objects.length >= 1) {
                if (ugandaEMRSyncService.validateFacility(objects[1].toString().split(cvsSplitBy)[VL_FACILITY_DHIS2_ID_CELL_NO].replaceAll("\"", ""))) {
                    healthCenterNameValidator = objects[1].toString().split(cvsSplitBy)[VL_FACILITY_NAME_CELL_NO].replaceAll("\"", "");

                    for (int i = 0; i < objects.length; i++) {
                        // use comma as separator
                        String[] vlResult = objects[i].toString().split(cvsSplitBy);


                        //When on headers of the CSV File
                        if (vlResult[VL_DATE_COLLECTION_CELL_NO].contentEquals("date_collected")) continue;

                        String vlDate = vlResult[VL_DATE_COLLECTION_CELL_NO].replaceAll("\"", "");
                        String patientARTNo = vlResult[VL_PATIENT_ART_ID_CELL_NO].replaceAll("\"", "");
                        String vlQuantitative = vlResult[VL_RESULTS_NUMERIC_CELL_NO].replaceAll("\"", "");
                        String vlQualitative = vlResult[VL_RESULTS_ALHPA_NUMERIC_CELL_NO].replaceAll("\"", "");
                        String dateFormat = "yyyy-MM-dd HH:mm:ss";
                        SimpleDateFormat formatter = new SimpleDateFormat(ugandaEMRSyncService.getDateFormat(vlDate));
                        SimpleDateFormat format = new SimpleDateFormat(dateFormat);

                        Date endDate = format.parse(new SimpleDateFormat("yyyy-MM-dd").format(formatter.parse(vlDate)) + " 23:59:59");

                        Date startDate = format.parse(new SimpleDateFormat("yyyy-MM-dd").format(formatter.parse(vlDate)) + " 00:00:00");


                        EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteria(ugandaEMRSyncService.getPatientByPatientIdentifier(patientARTNo), null, startDate, endDate, null, null, encounterTypes, null, null, null, false);
                        List<Encounter> encounters = new ArrayList<>();
                        //Determine if patient is found
                        if (encounterSearchCriteria.getPatient() != null) {
                            encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);

                            if (encounters.size() > 0) {
                                Order viralLoadOrder = null;
                                try{
                                for (Order order : encounters.get(0).getOrders()) {
                                    if (order.getConcept().getConceptId() == 165412 && order.isActive() && order.getInstructions()!=null && order.getInstructions().equals("REFER TO cphl")) {
                                        viralLoadOrder = order;
                                        break;
                                    }
                                }
                                }catch (Exception e){
                                    log.error("Error while searching for viralload order",e);
                                }
                                try {
                                    ugandaEMRSyncService.addVLToEncounter(vlQualitative, vlQuantitative, vlDate, encounters.get(0), viralLoadOrder);
                                } catch (Exception e) {
                                    log.error("Failed to add viral load to encounter", e);
                                }
                            } else {
                                this.noEncounterFound.add(vlResult[VL_PATIENT_ART_ID_CELL_NO]);
                            }
                        } else {
                            this.noPatientFound.add(vlResult[VL_PATIENT_ART_ID_CELL_NO]);
                        }
                    }
                    pageModel.put("errorMessage", "Success");
                } else {
                    healthCenterNameValidator = "Invalid Health Center DHIS2 UUID";
                    pageModel.put("errorMessage", "Health Center DHIS2 UUID not matching with one in EMR");
                }
            } else {
                pageModel.put("errorMessage", "No data found in the csv uploaded");
            }
//            pageModel.put("errorMessage", "CSV file has been uploaded successfully");
        } catch (Exception e) {
            pageModel.put("errorMessage", "Upload failed due to some errors, Contact System Administrator");
            log.error("Failed to read file", e);
        }
    }

}
